package ecd3;

import static service_setup.ThreadLocalProvider.getTransactionLog;

import ecd3.domain.Aggregate;
import ecd3.propa.MessageBuffer;
import ecd3.util.CopyUtil;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.PriorityBlockingQueue;
import service_setup.AccountRepo;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import service_setup.ThreadLocalProvider;

public class TransactionManagerImpl implements TransactionManager {

    private final Long replicaId;

    ConcurrentLinkedDeque<Transaction> transactionLog;
    Transaction runningTransaction;
    public EventualConsistentService eventualConsistentService;

    private final ReentrantLock lock;

    public TransactionManagerImpl(
            Long replicaId) {
        this.replicaId = replicaId;
        this.transactionLog = getTransactionLog(replicaId);
        this.lock = new ReentrantLock();
        this.eventualConsistentService = ThreadLocalProvider.getEventualConsistentService(replicaId);
    }

    @Override
    public Transaction getRunningTransaction() {
        // TODO: check whether or not the transaction here is really thread local and can't be overridden
        return runningTransaction;
    }

    @Override
    public Transaction bot() {
        lock.lock();
        try {
            Transaction transaction = new TransactionImpl();
            transaction.begin();
            runningTransaction = transaction;
            return transaction;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void commit(Transaction transaction) {
        lock.lock();
        try {
            // TODO: maybe include a check for the transaction to be the running transaction
            transaction.commit();
            transaction.getOperations().forEach(operation -> {
                eventualConsistentService.evalOperation(operation);
            });
            eventualConsistentService.addTransactionToTail(transaction);
            MessageBuffer.broadcast(replicaId, transaction);
            runningTransaction = null;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void rollback(Transaction transaction) {
        lock.lock();
        try {
            transaction.rollback(replicaId);
            runningTransaction = null;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean consumeBuffer(AccountRepo repository, ConcurrentLinkedQueue<Transaction> deque, long replicaId) {
        if (deque.isEmpty()) {
            return false;
        }
        while (!deque.isEmpty()) {
            Transaction transaction = deque.poll();
            if (transaction != null) {
                eventualConsistentService.addTransactionToTail(transaction);
                transaction.getOperations().forEach(eventualConsistentService::evalOperation);
            }
        }
//        Transaction transaction = deque.poll();
//        if (transaction != null) {
//            eventualConsistentService.addTransactionToTail(transaction);
//            transaction.getOperations().forEach(operation -> {
//                eventualConsistentService.evalOperation(operation);
//            });
//            return true;
//        }
        //eventualConsistentService.evaluateTransactionTail();
        return true;
    }
}
