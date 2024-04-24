package ecd3;

public interface TransactionManager {
        public Transaction getRunningTransaction();

        void bot();

        void commit();

        void abort();
}
