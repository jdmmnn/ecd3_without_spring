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
    Set<Aggregate<?>> readSet;
    Set<Aggregate<?>> writeSet;
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
    @SuppressWarnings("unchecked")
    public void rollback() {
        botSnapshot.get().forEach(aggregate -> {
            RepositoryManager.getInstance().getRepository(aggregate.getClass()).rollbackTo(aggregate, aggregate.getVersion());
        });
    }


    @Override
    public void logUpdateOperation(String aggregateName, String aggregateId, Object... parameters) {
        operations.add(new Operation(aggregateName, aggregateId, parameters));
    }

    @Override
    public void logReadOperation(Aggregate<?> aggregate) {
        readSet.add(aggregate);
    }

    @Override
    public void logWriteOperation(Aggregate<?> aggregate) {
        writeSet.add(aggregate);
    }


    public int getId() {
        return id;
    }

    public int getReplicaId() {
        return replicaId;
    }

    public Long getBotTimeStamp() {
        return botTimeStamp;
    }

    public Long getCommitTimeStamp() {
        return commitTimeStamp;
    }

    public Snapshot getBotSnapshot() {
        return botSnapshot;
    }

    public Snapshot getCommitSnapshot() {
        return commitSnapshot;
    }

    public Set<Aggregate<?>> getReadSet() {
        return readSet;
    }

    public Set<Aggregate<?>> getWriteSet() {
        return writeSet;
    }

    public List<Operation> getOperations() {
        return operations;
    }
}
