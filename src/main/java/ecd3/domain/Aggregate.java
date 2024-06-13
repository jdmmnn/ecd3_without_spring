package ecd3.domain;

import ecd3.Transaction;
import ecd3.TransactionManager;
import service_setup.ThreadLocalProvider;

import java.io.Serializable;

public abstract class Aggregate<A> implements Serializable {

    transient TransactionManager transactionManager = ThreadLocalProvider.getTransactionManager(Thread.currentThread().threadId());

    public String id;

    int version;
    int replicaId;

    public Aggregate() {
        this.id = java.util.UUID.randomUUID().toString();
        this.version = 0;
        this.replicaId = ThreadLocalProvider.getReplicaId();
    }

    public String getId() {
        return id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void incrementVersion() {
        version++;
    }

    public int getReplicaId() {
        return replicaId;
    }

    public void logReadOperation(Transaction transaction) {
        // TODO: implement the read set mechanic
        //transaction.logReadOperation(this);
    }

    public void logWriteOperation(Transaction transaction) {
        // TODO: implement the write set mechanic
        //transaction.logWriteOperation( this);
    }

    public void logOperation(Transaction transaction, OperationEnum crud, Object... parameters) {
        transaction.logOperation(this.getClass().getSimpleName(), id, crud, parameters);
    }
}
