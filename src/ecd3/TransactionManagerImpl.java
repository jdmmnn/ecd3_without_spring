package ecd3;

import java.util.Deque;
import java.util.List;
import java.util.Set;

public class TransactionManagerImpl implements TransactionManager {

    Deque<Transaction> volatileTransactionTail;
    List<Transaction> transactionLog;

    @Override
    public Transaction createTransaction(Set<Aggregate> readSet, Set<Aggregate> writeSet, int replicaId) {
        volatileTransactionTail.
        return null;
    }

    @Override
    public Transaction getRunningTransaction() {
        return null;
    }
}
