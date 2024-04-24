package ecd3;

import java.io.Serializable;

public abstract class Aggregate<A extends Aggregate<A>> implements Serializable {

    TransactionManager transactionManager = TransactionManagerImpl.getInstance();

    String id;

    int version;

    public String getId() {
        return id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    // increment version
    void incrementVersion() {
        version++;
    }

    protected void logReadOperation() {
        Transaction runningTransaction = transactionManager.getRunningTransaction();
        runningTransaction.logReadOperation(this);
    }

    protected void logWriteOperation() {
        Transaction runningTransaction = transactionManager.getRunningTransaction();
        runningTransaction.logWriteOperation(this);
    }

    protected void logOperation(Object... parameters) {
        Transaction runningTransaction = transactionManager.getRunningTransaction();
        runningTransaction.logWriteOperation(this);
        runningTransaction.logUpdateOperation(getClass().getName(), this.getId(), parameters);
    }
}
