package ecd3;

import service_setup.PersonRepo;

public interface TransactionManager {
        public Transaction getRunningTransaction();

        Transaction bot();

        void commit(Transaction transaction);

        void rollback(Transaction transaction);

        void consumeBuffer();
}
