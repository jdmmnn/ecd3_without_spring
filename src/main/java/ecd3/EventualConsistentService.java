package ecd3;

import ecd3.domain.Operation;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Stream;
import service_setup.Account;
import service_setup.AccountRepo;
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
            transaction.getOperations().forEach(this::evalOperation);
            if (transaction.getCommitTimeStamp() < Instant.now().toEpochMilli() - TRANSACTION_TAIL_REMAIN_TIME) {
                transactionTail.remove(transaction);
                transactionLog.add(transaction);
            }
        });
    }

    public void evalOperation(Operation e) {
        switch (e.cud) {
            case CREATE -> create(e);
            case UPDATE -> update(e);
            case DELETE -> delete(e);
            case WITHDRAW -> withdraw(e);
            case DEPOSIT -> deposit(e);
        }
    }

    private void create(Operation e) {
        for (Object arg : e.args) {
            repo.save((Account) arg);
        }
    }

    private void update(Operation e) {
        for (Object arg : e.args) {
            repo.update((Account) arg);
        }
    }

    private void delete(Operation e) {
        for (Object arg : e.args) {
            repo.delete((Account) arg);
        }
    }

    private void withdraw(Operation e) {
        for (Object arg : e.args) {
            repo.update((Account) arg);
        }
    }

    private void deposit(Operation e) {
        for (Object arg : e.args) {
            repo.update((Account) arg);
        }
    }

    public void addTransactionToTail(Transaction transaction) {
        transactionTail.add(transaction);
    }
}
