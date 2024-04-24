package ecd3;

import java.util.*;
import java.util.stream.Collectors;

public class TransactionManagerImpl implements TransactionManager {

    private static TransactionManager INSTANCE;

    private static final int TRANSACTION_TAIL_REMAIN_TIME = 1000;

    List<Transaction> volatileTransactionTail = new ArrayList<>();
    Deque<Transaction> transactionLog = new ArrayDeque<>();
    ThreadLocal<Transaction> runningTransaction = new ThreadLocal<>();

    public static TransactionManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TransactionManagerImpl();
        }
        return INSTANCE;
    }

    @Override
    public Transaction getRunningTransaction() {
        return runningTransaction.get();
    }

    @Override
    public void bot() {
        Transaction transaction = new TransactionImpl();
        transaction.begin();
        runningTransaction.set(transaction);
    }

    @Override
    public void commit() {
        Transaction transaction = runningTransaction.get();
        Set<Aggregate<?>> allInWriteAggregates = getAllInWriteAggregates();
        volatileTransactionTail.add(transaction);
        if (allInWriteAggregates.containsAll(transaction.getWriteSet())) {
            // TODO: check the resolution algorithm that resolves the problem of multiple writes on the same object
            reorderVolatileTransactionTail();
        }
        transaction.commit();
        runningTransaction.remove();
    }

    @Override
    public void abort() {
        // TODO: check how to rollback the transaction, specific how the entity is set to the old state. (maybe multiversion?)
        Transaction transaction = runningTransaction.get();
        transaction.rollback();
        runningTransaction.remove();
    }

    private Set<Aggregate<?>> getAllInWriteAggregates() {
        return volatileTransactionTail.stream().map(Transaction::getWriteSet).flatMap(Set::stream).collect(Collectors.toSet());
    }




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
}
