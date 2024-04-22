package ecd3;

public interface Transaction {

    public void begin();
    public void commit();
    public void rollback();
    public void logUpdateOperation(Operation operation);
}
