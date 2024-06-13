package service_setup;

import ecd3.Transaction;
import ecd3.TransactionManager;
import ecd3.propa.MessageBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

public class SynchronisationWorker extends Thread {

    public final Long replicaId;
    public final ServiceReplica serviceReplica;
    public final AccountRepo accountRepo;
    public final TransactionManager transactionManager;
    public final ConcurrentLinkedQueue<Transaction> buffer;
    public final ConcurrentSkipListSet<Transaction> transactionTail;

    public SynchronisationWorker(
            String name,
            long replicaId) {
        super(name);
        this.serviceReplica = ThreadLocalProvider.getReplicaThread(replicaId);
        this.replicaId = replicaId;
        this.accountRepo = ThreadLocalProvider.getAccountRepo(replicaId);
        this.transactionManager = ThreadLocalProvider.getTransactionManager(replicaId);
        this.buffer = MessageBuffer.registerOrGet(replicaId);
        this.transactionTail = ThreadLocalProvider.getTransactionTail(replicaId);
    }

    @Override
    public void run() {
        int count = 0;
        while ((serviceReplica.isRunning() && !transactionTail.isEmpty()) || (serviceReplica.isRunning() && count < 1000)) {
            if (!transactionManager.consumeBuffer(accountRepo, buffer, replicaId)) {
                count++;
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                count = 0;
            }
        }
    }
}
