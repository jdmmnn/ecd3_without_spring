package ecd3;

import ecd3.domain.Operation;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Stream;
import service_setup.Account;
import service_setup.AccountAllReadyExistsException;
import service_setup.AccountRepo;
import service_setup.InsufficientFundsException;
import service_setup.NoAccountFoundException;
import service_setup.ThreadLocalProvider;

public class EventualConsistentService {

    private final Long replicaId;

    private static final int TRANSACTION_TAIL_REMAIN_TIME_MILLIS = 10000000;
    ConcurrentSkipListSet<Transaction> transactionTail;

    private final ConcurrentLinkedDeque<Transaction> transactionLog;

    public final AccountRepo repo;

    public EventualConsistentService(
            Long replicaId) {
        this.replicaId = replicaId;
        this.transactionTail = ThreadLocalProvider.getTransactionTail(replicaId);
        this.transactionLog = ThreadLocalProvider.getTransactionLog(replicaId);
        this.repo = ThreadLocalProvider.getAccountRepo(replicaId);
    }

    public void eval(Transaction transaction) {
        transaction.getOperations().forEach(operation -> evaluateOperation(operation, transaction));
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


    public void addTransactionToTail(Transaction transaction) {
        transactionTail.add(transaction);
        if (transactionTail.last().getCommitTimeStamp().compareTo(transaction.getCommitTimeStamp()) > 0) {
            Transaction lower = transactionTail.lower(transaction);
            NavigableSet<Transaction> transactions;
            if (lower != null) {
                transactions = transactionTail.tailSet(lower, true);
            } else {
                transactions = transactionTail.tailSet(transaction, true);
            }
            transactions.descendingSet().forEach(repo::rollbackToTransaction);
            transactions.forEach(this::eval);

        } else {
            eval(transaction);
        }
    }


    public void evaluateOperation(Operation e, Transaction transaction) {
        try {
            evalOperation(e);
        } catch (RepositoryException ex) {
            transaction.rollback();
            ex.printStackTrace();
        }
    }

    public void rollback(Transaction transaction) throws CanNotRollBackException {
        if (transactionLog.contains(transaction)) {
            throw new CanNotRollBackException();
        }
        repo.rollbackToTransaction(transaction);
        transactionTail.tailSet(transaction).forEach(this::eval);
        transactionTail.remove(transaction);
    }

    public void cleanUpTransactionTail() {
        for (Transaction transaction : transactionTail) {
            Instant now = Instant.now().minusMillis(TRANSACTION_TAIL_REMAIN_TIME_MILLIS);
            if (transaction.getCommitTimeStamp().compareTo(now) <= 0) {
                transactionTail.remove(transaction);
                transactionLog.add(transaction);
            }
        }
    }

    private void create(Operation e) throws AccountAllReadyExistsException {
        Account account = (Account) e.args[0];
//        if (repo.persistence().containsKey(account.getName())) {
//            throw new AccountAllReadyExistsException("Account with name " + account.getName() + " already exists");
//        }
        repo.save(account);
    }

    private void update(Operation e) throws NoAccountFoundException {
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
            repo.update(foundAccount);
        }
    }

    private void deposit(Operation e) throws NoAccountFoundException {
        Account account = (Account) e.args[0];
        double amount = (double) e.args[1];

        Account foundAccount = repo.findByName(account.getName())
                .orElseThrow(() -> new NoAccountFoundException(account.getName() + " in " + repo.persistence()));
        if (foundAccount.getBalance() + amount < 0) {
            throw new InsufficientFundsException("insuficient funds for account: " + foundAccount.getName());
        } else {
            foundAccount.setBalance(account.getBalance() + amount);
            repo.update(foundAccount);
        }
    }
}
