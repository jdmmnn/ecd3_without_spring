package ecd3;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public class TransactionImpl implements Transaction {

    int id;
    int replicaId;
    Long botTimeStamp;
    Long commitTimeStamp;
    Snapshot botSnapshot;
    Snapshot commitSnapshot;
    Set<Aggregate> readSet;
    Set<Aggregate> writeSet;
    List<Operation> operations;


    @Override
    public void begin() {
        botTimeStamp = Instant.now().toEpochMilli();
        botSnapshot = new SnapshotImpl(writeSet);
    }

    @Override
    public void commit() {
        commitTimeStamp = Instant.now().toEpochMilli();
        commitSnapshot = new SnapshotImpl(writeSet);
    }

    @Override
    public void rollback() {
        botSnapshot = commitSnapshot;
    }

    @Override
    public void logUpdateOperation(Operation operation) {
        operations.add(operation);
    }
}
