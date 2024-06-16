package service_setup;

import ecd3.CanNotRollBackException;
import ecd3.EventualConsistentService;
import ecd3.Transaction;
import ecd3.TransactionManager;
import ecd3.propa.MessageBuffer;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

public class SynchronisationWorker extends Thread {

    public final Long replicaId;
    public final TransactionManager transactionManager;
    public final EventualConsistentService eventualConsistentService;

    public SynchronisationWorker(
            String name,
            long replicaId) {
        super(name);
        this.replicaId = replicaId;
        this.transactionManager = ThreadLocalProvider.getTransactionManager(replicaId);
        eventualConsistentService = ThreadLocalProvider.getEventualConsistentService(replicaId);
    }

    @Override
    public void run() {
        boolean running = true;
        int count = 0;
        while (running || count <= 5) {
            running = transactionManager.consumeBuffer();
            if (!running) {
                count++;
            }
            eventualConsistentService.cleanUpTransactionTail();
        }
    }
}
