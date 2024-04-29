package ecd3;

import service_setup.Person;
import service_setup.PersonRepo;
import service_setup.ThreadLocalProvider;

import java.io.Serializable;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TransactionImpl implements Transaction {

    int id;
    int replicaId;
    Long botTimeStamp;
    Long commitTimeStamp;
    Snapshot botSnapshot; // TODO: snapshots DO NOT WORK!!! currently. Need to fix this.
    Snapshot commitSnapshot;
    Set<Aggregate<?>> readSet = new HashSet<>();
    Set<Aggregate<?>> writeSet = new HashSet<>();
    List<Operation> operations = new ArrayList<>();

    public TransactionImpl() {
        this.id = ThreadLocalProvider.getTransactionId().getAndIncrement();
        this.replicaId = ThreadLocalProvider.getReplicaId();
    }

    @Override
    public void begin() {
        botTimeStamp = Instant.now().toEpochMilli();
        //botSnapshot = new SnapshotImpl(writeSet);
    }

    @Override
    public void commit() {
        commitTimeStamp = Instant.now().toEpochMilli();
        commitSnapshot = new SnapshotImpl(writeSet);
    }

    @Override
    public void rollback() {
        botSnapshot.get().forEach(aggregate -> ThreadLocalProvider.getPersonRepo().get().rollbackTo((Person) aggregate, aggregate.getVersion()));
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

    @Override
    public String toString() {
        return "TransactionImpl{" +
                "replicaId=" + replicaId +
                ", id=" + id +
                ", operations=" + operations.getFirst().args.toString() +
                '}';
    }
}
