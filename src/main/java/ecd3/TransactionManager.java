package ecd3;

import java.util.concurrent.ConcurrentLinkedQueue;
import service_setup.AccountRepo;

public interface TransactionManager {
        public Transaction getRunningTransaction();

        Transaction bot();

        void commit(Transaction transaction);

        void rollback(Transaction transaction);

        boolean consumeBuffer();

        boolean consumeBuffer(AccountRepo repository, ConcurrentLinkedQueue<Transaction> deque, long replicaId)
                throws CanNotRollBackException;
}
