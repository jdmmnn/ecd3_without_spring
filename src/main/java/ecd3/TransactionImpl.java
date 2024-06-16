package ecd3;

import ecd3.domain.Aggregate;
import ecd3.domain.OperationEnum;
import ecd3.domain.Operation;
import ecd3.propa.MessageBuffer;
import service_setup.ThreadLocalProvider;

import java.time.Instant;
import java.util.*;

public class TransactionImpl implements Transaction {

    int id;
    Long replicaId;
    Instant botTimeStamp;
    Instant commitTimeStamp;
    public Snapshot botSnapshot; // TODO: snapshots DO NOT WORK!!! currently. Need to fix this.
    Snapshot commitSnapshot;
    Set<Aggregate<?>> readSet = new HashSet<>();
    Set<Aggregate<?>> writeSet = new HashSet<>();
    List<Operation> operations = new ArrayList<>();
    boolean rollback = false;
    public boolean isEnd = false;

    public TransactionImpl(Long replicaId) {
        this.id = ThreadLocalProvider.getTransactionId().getAndIncrement();
        this.replicaId = replicaId;
    }

    @Override
    public void begin(Map<String, Integer> botVersions) {
        botTimeStamp = Instant.now();
        botSnapshot = new SnapshotImpl(botVersions);
    }

    @Override
    public void commit(Map<String, Integer> commitVersions) {
        commitTimeStamp = Instant.now();
        commitSnapshot = new SnapshotImpl(commitVersions);
    }

    @Override
    public void rollback() {
        this.rollback = true;
        Long replicaIdByThread = ThreadLocalProvider.getReplicaIdByThread(Thread.currentThread());
        if (!replicaId.equals(replicaIdByThread)) {
            MessageBuffer.broadcast(replicaId, this);
            System.err.println("rollback of transaction " + this.getId() + " by replica " + replicaIdByThread);
        }
    }

    @Override
    public void logOperation(String aggregateName, String aggregateId, OperationEnum operation, Object... parameters) {
        Operation e = new Operation(aggregateName, aggregateId, operation, parameters);
        operations.add(e);
    }

//    @Override
//    public void logReadOperation(Aggregate<?> aggregate) {
//        readSet.add(aggregate);
//    }

    @Override
    public void logWriteOperation(Aggregate<?> aggregate) {
        writeSet.add(aggregate);
    }


    public int getId() {
        return id;
    }

    public Long getReplicaId() {
        return replicaId;
    }

    public Instant getBotTimeStamp() {
        return botTimeStamp;
    }

    public Instant getCommitTimeStamp() {
        return commitTimeStamp;
    }

    public void setCommitTimeStamp(Instant instant) {
        this.commitTimeStamp = instant;
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

    public boolean isRollback() {
        return this.rollback;
    }

    @Override
    public Snapshot getBotSnapShot() {
        return this.botSnapshot;
    }

    @Override
    public void setWasPropagated() {
        isEnd = true;
    }

    @Override
    public boolean getIsEnd() {
        return isEnd;
    }

    @Override
    public String toString() {
        return "TransactionImpl{" +
                "producerReplicaId=" + replicaId +
                ", id=" + id +
                ", operations=" + (!getOperations().isEmpty() ? Arrays.toString(new List[]{getOperations()}) : "-") +
                '}';
    }
}
