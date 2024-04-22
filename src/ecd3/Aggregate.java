package ecd3;

import java.io.Serializable;

public abstract class Aggregate implements Serializable {

    protected void logOperation(Object... parameters) {
        Transaction runningTransaction = TransactionManager.getRunningTransaction();
        runningTransaction.logUpdateOperation();
    }
}
