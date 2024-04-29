package ecd3;

import ecd3.propa.MessageBuffer;
import service_setup.Person;
import service_setup.PersonRepo;
import service_setup.ThreadLocalProvider;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class TransactionManagerImpl implements TransactionManager {

    private static final int TRANSACTION_TAIL_REMAIN_TIME = 100000;

    List<Transaction> volatileTransactionTail = new ArrayList<>();
    Deque<Transaction> transactionLog = new ArrayDeque<>();
    Transaction runningTransaction;

    private final ReentrantLock lock = new ReentrantLock();

    public TransactionManagerImpl() {
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
            Set<Aggregate<?>> allInWriteAggregates = getAllInWriteAggregates();
            transaction.commit();
            volatileTransactionTail.add(transaction);
            if (allInWriteAggregates.containsAll(transaction.getWriteSet())) {
                // TODO: check the resolution algorithm that resolves the problem of multiple writes on the same object
                reorderVolatileTransactionTail();
            }
            System.err.println("commit From Thread: " + Thread.currentThread());
            MessageBuffer.broadcast(Thread.currentThread(), CopyUtil.deepCopy(transaction));
            runningTransaction = null;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void rollback(Transaction transaction) {
        lock.lock();
        try {
            transaction.rollback();
            runningTransaction = null;
        } finally {
            lock.unlock();
        }
    }

    private Set<Aggregate<?>> getAllInWriteAggregates() {
        return volatileTransactionTail.stream().map(Transaction::getWriteSet).flatMap(Set::stream).collect(Collectors.toSet());
    }

    // TODO: check whether or not this is enough as a reordering algorithm
    public void reorderVolatileTransactionTail() {
        volatileTransactionTail.sort(Comparator.comparing(Transaction::getCommitTimeStamp));
        Map<String, String> toChangeAggregateIdMap = new HashMap<>();
        volatileTransactionTail.forEach(transaction -> {
            transaction.getWriteSet().forEach(aggregate -> {
                toChangeAggregateIdMap.put(aggregate.getClass().getName(), aggregate.getId());
            });
        });
    }

    public void cleanUp() {
        long currentTime = System.currentTimeMillis();
        for (Transaction transaction : volatileTransactionTail) {
            if (currentTime - transaction.getCommitTimeStamp() > TRANSACTION_TAIL_REMAIN_TIME) {
                transactionLog.offer(transaction);
            }
        }
    }

    @Override
    public void consumeBuffer() {
        Thread thread = Thread.currentThread();
        System.err.println("Consuming buffer for Thread: " + thread);
        ConcurrentLinkedQueue<Transaction> buffer = MessageBuffer.getBuffer(thread);
        System.err.println("Buffer ID: " + System.identityHashCode(buffer));
        Transaction transaction = buffer.poll();
        if (transaction != null) {
            transaction.getOperations().forEach(operation -> {
                // reexecute each operation
                int replicaId = transaction.getReplicaId();
                for (Object arg : operation.args) {
                    ThreadLocalProvider.getPersonRepo().get().save((Person) arg);
                }
            });
            volatileTransactionTail.add(transaction);
            reorderVolatileTransactionTail();
            cleanUp();
        }
    }
}
