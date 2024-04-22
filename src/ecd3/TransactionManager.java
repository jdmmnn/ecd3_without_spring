package ecd3;

import java.util.Set;

public interface TransactionManager {
    public Transaction createTransaction(Set<Aggregate> readSet, Set<Aggregate> writeSet, int replicaId);

    public Transaction getRunningTransaction();
}
