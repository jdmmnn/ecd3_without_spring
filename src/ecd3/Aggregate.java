package ecd3;

import service_setup.ThreadLocalProvider;

import java.io.Serializable;

public abstract class Aggregate<A> implements Serializable {

    transient ThreadLocal<TransactionManager> transactionManager = ThreadLocalProvider.getTransactionManager();

    public String id;

    int version;

    public Aggregate() {
        this.id = java.util.UUID.randomUUID().toString();
        this.version = 0;
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

    void incrementVersion() {
        version++;
    }

    protected void logReadOperation() {
        System.err.println("logReadOperation");
        System.err.println("this: " + this);
        System.err.printf("Transaction Manager ID: %s in Thread: %s%n", System.identityHashCode(transactionManager), Thread.currentThread().getName());
        Transaction runningTransaction = transactionManager.get().getRunningTransaction();
        runningTransaction.logReadOperation(this);
    }

    protected void logWriteOperation() {
        System.err.println("logWriteOperation");
        System.err.println("this: " + this);
        System.err.printf("Transaction Manager ID: %s in Thread: %s%n", System.identityHashCode(transactionManager), Thread.currentThread().getName());
        Transaction runningTransaction = transactionManager.get().getRunningTransaction();
        runningTransaction.logWriteOperation(this);
    }

    protected void logOperation(Object... parameters) {
        System.err.println("logOperation");
        System.err.println("this: " + this);
        System.err.printf("Transaction Manager ID: %s in Thread: %s%n", System.identityHashCode(transactionManager), Thread.currentThread().getName());
        Transaction runningTransaction = transactionManager.get().getRunningTransaction();
        runningTransaction.logWriteOperation(this);
        runningTransaction.logUpdateOperation(getClass().getName(), this.getId(), parameters);
    }
}
