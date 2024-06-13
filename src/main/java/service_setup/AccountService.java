package service_setup;

import ecd3.Transaction;
import ecd3.TransactionManager;
import ecd3.domain.Operation;
import ecd3.domain.OperationEnum;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class AccountService {

    public final Long replicaId;

    public TransactionManager transactionManager;

    public AccountRepo accountRepo;

    public AccountService(Long replicaId) {
        this.replicaId = replicaId;
        this.transactionManager = ThreadLocalProvider.getTransactionManager(replicaId);
        this.accountRepo = ThreadLocalProvider.getAccountRepo(replicaId);
    }

    public Account createAccount(String name, double initialBalance) {
        Transaction transaction = transactionManager.bot();
        Account account = new Account(name, initialBalance);
        account.logWriteOperation(transaction);
        account.logOperation(transaction, OperationEnum.CREATE, account);
        ThreadLocalProvider.log(ThreadLocalProvider.getReplicaId(),
                account.getReplicaId(),
                account.getId(),
                transaction.getId(),
                OperationEnum.CREATE,
                Instant.now());
        transactionManager.commit(transaction);
        return account;
    }

    // withdraw money from account
    public void withdrawMoney(String name, double amount) throws InsufficientFundsException, NoAccountFoundException {
        Transaction transaction = transactionManager.bot();
        Account account = accountRepo.findByName(name).orElseThrow(() -> new NoAccountFoundException(name + " in " + accountRepo.persistence()));
        account.logReadOperation(transaction);
        if (account.getBalance() < amount)
            throw new InsufficientFundsException("Insufficient funds");
        account.logOperation(transaction, OperationEnum.WITHDRAW, account, amount);
        ThreadLocalProvider.log(ThreadLocalProvider.getReplicaId(),
                account.getReplicaId(),
                account.getId(),
                transaction.getId(),
                OperationEnum.WITHDRAW,
                Instant.now());
        transactionManager.commit(transaction);
    }

    // deposit money to account
    public void depositMoney(String name, double amount) throws NoAccountFoundException {
        Transaction transaction = transactionManager.bot();
        Account account = accountRepo.findByName(name).orElseThrow(() -> new NoAccountFoundException(name + " in " + accountRepo.persistence()));        account.logReadOperation(transaction);
        account.logOperation(transaction, OperationEnum.DEPOSIT, account, amount);
        ThreadLocalProvider.log(ThreadLocalProvider.getReplicaId(),
                account.getReplicaId(),
                account.getId(),
                transaction.getId(),
                OperationEnum.DEPOSIT,
                Instant.now());
        transactionManager.commit(transaction);
    }



    public void transferMoney(String fromName, String toName, double amount) throws InsufficientFundsException, NoAccountFoundException {
        Transaction transaction = transactionManager.bot();
        Account fromAccount = accountRepo.findByName(fromName).orElseThrow(() -> new NoAccountFoundException(fromName + " in " + accountRepo.persistence()));
        Account toAccount = accountRepo.findByName(toName).orElseThrow(() -> new NoAccountFoundException(toName + " in " + accountRepo.persistence()));
        fromAccount.logReadOperation(transaction);
        toAccount.logReadOperation(transaction);
        if (fromAccount.getBalance() < amount)
            throw new InsufficientFundsException("Insufficient funds");
        fromAccount.setBalance(fromAccount.getBalance() - amount);
        toAccount.setBalance(toAccount.getBalance() + amount);
        fromAccount.logOperation(transaction, OperationEnum.WITHDRAW, fromAccount);
        toAccount.logOperation(transaction, OperationEnum.DEPOSIT, toAccount);
        ThreadLocalProvider.log(ThreadLocalProvider.getReplicaId(),
                fromAccount.getReplicaId(),
                fromAccount.getId(),
                transaction.getId(),
                OperationEnum.WITHDRAW,
                Instant.now());
        ThreadLocalProvider.log(ThreadLocalProvider.getReplicaId(),
                toAccount.getReplicaId(),
                toAccount.getId(),
                transaction.getId(),
                OperationEnum.DEPOSIT,
                Instant.now());
        transactionManager.commit(transaction);
    }

    public void updatePerson(String id, String name, int newBalance) {
        Transaction transaction = transactionManager.bot();
        Account account = accountRepo.findById(id);
        account.logReadOperation(transaction);
        account.setName(name);
        account.setBalance(newBalance);
        accountRepo.save(account);
        account.logOperation(transaction, OperationEnum.UPDATE, account);
        ThreadLocalProvider.log(ThreadLocalProvider.getReplicaId(),
                account.getReplicaId(),
                account.getId(),
                transaction.getId(),
                OperationEnum.UPDATE,
                Instant.now());
        transactionManager.commit(transaction);
    }

    public void deleteAccount(String id) {
        Transaction transaction = transactionManager.bot();
        Account account = accountRepo.findById(id);
        accountRepo.delete(account);
        account.logOperation(transaction, OperationEnum.DELETE, account);
        ThreadLocalProvider.log(ThreadLocalProvider.getReplicaId(),
                account.getReplicaId(),
                account.getId(),
                transaction.getId(),
                OperationEnum.DELETE,
                Instant.now());
        transactionManager.commit(transaction);
    }

    public Account getPerson(String id) {
        return accountRepo.findById(id);
    }

    public Optional<Account> findPerson(String name) {
        return accountRepo.findByName(name);
    }
}
