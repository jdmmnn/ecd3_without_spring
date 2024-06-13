package ecd3;

import ecd3.Repository.Version;
import ecd3.domain.Operation;
import java.time.Instant;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentSkipListSet;
import service_setup.Account;
import service_setup.AccountRepo;
import service_setup.InsufficientFundsException;
import service_setup.NoAccountFoundException;
import service_setup.ThreadLocalProvider;

public class EventualConsistentService {

    private final Long replicaId;

    private static final int TRANSACTION_TAIL_REMAIN_TIME = 10000;
    ConcurrentSkipListSet<Transaction> transactionTail;

    private final ConcurrentLinkedDeque<Transaction> transactionLog;

    private final AccountRepo repo;

    public EventualConsistentService(
            Long replicaId) {
        this.replicaId = replicaId;
        this.transactionTail = ThreadLocalProvider.getTransactionTail(replicaId);
        this.transactionLog = ThreadLocalProvider.getTransactionLog(replicaId);
        this.repo = ThreadLocalProvider.getAccountRepo(replicaId);
    }

    /*
     * This method is called by the service replica to evaluate the transaction tail
     * it will evaluate the transaction tail and apply the transactions to the repository as versioned objects
     * it will reset the versions of the objects in case of a conflict and will apply the transactions in the correct order
     * it will resolve the conflicts in case of multiple writes on the same object
     */
    public void evaluateTransactionTail() {
        transactionTail.forEach(transaction -> {
            transaction.getOperations().forEach(e -> {
                try {
                    evalOperation(e);
                } catch (InsufficientFundsException | NoAccountFoundException | AccountAllReadyExistsException ex) {
                    throw new RuntimeException(ex);
                } finally {
                    transaction.rollback();
                }
            });
            if (transaction.getCommitTimeStamp() < Instant.now().toEpochMilli() - TRANSACTION_TAIL_REMAIN_TIME) {
                transactionTail.remove(transaction);
                transactionLog.add(transaction);
            }
        });
    }

    public void evalOperation(Operation e) throws InsufficientFundsException, NoAccountFoundException, AccountAllReadyExistsException {
        switch (e.cud) {
            case CREATE -> create(e);
            case UPDATE -> update(e);
            case DELETE -> delete(e);
            case WITHDRAW -> withdraw(e);
            case DEPOSIT -> deposit(e);
        }
    }

    private void create(Operation e) throws AccountAllReadyExistsException {
        Account account = (Account) e.args[0];
        if (repo.persistence().containsKey(account.getName())) {
            throw new AccountAllReadyExistsException("Account with name " + account.getName() + " already exists");
        }
        repo.save(account);
    }

    private void update(Operation e) {
        for (Object arg : e.args) {
            repo.update((Account) arg);
        }
    }

    private void delete(Operation e) throws NoAccountFoundException {
        Account account = (Account) e.args[0];
        repo.findByName(account.getName()).orElseThrow(() -> new NoAccountFoundException(account.getName() + " in " + repo.persistence()));
        repo.delete(account);
    }

    private void withdraw(Operation e) throws InsufficientFundsException, NoAccountFoundException {
        Account account = (Account) e.args[0];
        double amount = (double) e.args[1];
        Account foundAccount = repo.findByName(account.getName())
                .orElseThrow(() -> new NoAccountFoundException(account.getName() + " in " + repo.persistence()));
        if (foundAccount.getBalance() < amount) {
            throw new InsufficientFundsException("Insufficient funds");
        } else {
            foundAccount.setBalance(foundAccount.getBalance() - amount);
            if (!repo.multiVersionPersistence().containsKey(new Version<String>(foundAccount.id, foundAccount.getVersion()))) {
                repo.update(foundAccount);
            }
        }
    }

    private void deposit(Operation e) throws NoAccountFoundException {
        Account account = (Account) e.args[0];
        double amount = (double) e.args[1];
        repo.findByName(account.getName()).orElseThrow(() -> new NoAccountFoundException(account.getName() + " in " + repo.persistence()));
        if (!repo.multiVersionPersistence().containsKey(new Version<String>(account.id, account.getVersion()))) {
            account.setBalance(account.getBalance() + amount);
            repo.update(account);
        }
    }

    public void addTransactionToTail(Transaction transaction) {
        transactionTail.add(transaction);
    }
}
