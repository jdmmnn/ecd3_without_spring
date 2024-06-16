package ecd3;

import ecd3.propa.MessageBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import service_setup.AccountRepo;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;
import service_setup.ThreadLocalProvider;

public class TransactionManagerImpl implements TransactionManager {

    private final Long replicaId;

    Transaction runningTransaction;
    public EventualConsistentService eventualConsistentService;
    private BlockingDeque<Transaction> buffer;
    List<Instant> bufferTimeStamp = new ArrayList<>();

    private final ReentrantLock lock;

    public TransactionManagerImpl(
            Long replicaId) {
        this.replicaId = replicaId;
        this.lock = new ReentrantLock();
        this.eventualConsistentService = ThreadLocalProvider.getEventualConsistentService(replicaId);
        this.buffer = MessageBuffer.registerOrGet(replicaId);
    }

    @Override
    public Transaction getRunningTransaction() {
        // TODO: check whether or not the transaction here is really thread local and can't be overridden
        return runningTransaction;
    }

    @Override
    public Transaction bot() {
        lock.lock();
        try {
            Transaction transaction = new TransactionImpl(replicaId);
            transaction.begin(ThreadLocalProvider.getAccountRepo(replicaId).getSnapshotVersions());
            runningTransaction = transaction;
            return transaction;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void commit(Transaction transaction) {
        lock.lock();
        try {
            // TODO: maybe include a check for the transaction to be the running transaction
            transaction.commit(eventualConsistentService.repo.getSnapshotVersions());
//            eventualConsistentService.addTransactionToTail(transaction);
            MessageBuffer.broadcast(replicaId, transaction);
            runningTransaction = null;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void rollback(Transaction transaction) {
//        lock.lock();
//        try {
//            transaction.rollback();
//            runningTransaction = null;
//        } finally {
//            lock.unlock();
//        }
    }

    @Override
    public boolean consumeBuffer() {
        try {
            Transaction transaction = buffer.take();
            if (transaction.isRollback()) {
                eventualConsistentService.rollback(transaction);
            } else {
                eventualConsistentService.addTransactionToTail(transaction);
            }
        } catch (InterruptedException | CanNotRollBackException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public boolean consumeBuffer(AccountRepo repository, ConcurrentLinkedQueue<Transaction> deque, long replicaId)
            throws CanNotRollBackException {
        return false;
    }
}
