package ecd3;

public class RollbackServiceImpl implements RollbackService {

    

    @Override
    public void rollback(Transaction transaction) {
        if (transaction == null) {
            throw new NoTransactionRunningException();
        }
//        transaction.rollback();
    }
}
