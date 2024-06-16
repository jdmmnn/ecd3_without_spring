package service_setup;

import ecd3.Transaction;
import ecd3.TransactionManager;
import ecd3.propa.MessageBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import service_setup.testing.Task;


public class ServiceReplica extends Thread {

    public Long replicaId = -1l;
    private final AtomicBoolean isRunning = new AtomicBoolean(true);
    private final BlockingQueue<Task> taskQueue;
    public AccountRepo accountRepo;
    public AccountService accountService;
    public TransactionManager transactionManager;

    public ServiceReplica(String name, BlockingQueue<Task> taskQueue) {
        super(name);
        this.taskQueue = taskQueue;
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    @Override
    public void run() {
        ThreadLocalProvider.reset();
        MessageBuffer.reset();
        System.gc();
        try {
            sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        replicaId = (long) ThreadLocalProvider.getReplicaId();
        ThreadLocalProvider.registerReplicaThread(replicaId, this);
        ThreadLocalProvider.registerReplicaIdWithThread(replicaId, this);
        accountRepo = ThreadLocalProvider.getAccountRepo(replicaId);
        transactionManager = ThreadLocalProvider.getTransactionManager(replicaId);
        accountService = ThreadLocalProvider.getPersonService(replicaId);


        SynchronisationWorker synchronisationWorker = new SynchronisationWorker(
                "SynchroReplica" + this.getName(),
                replicaId
        );
        ThreadLocalProvider.registerReplicaIdWithThread(replicaId, synchronisationWorker);
        synchronisationWorker.start();

        try {
            sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        while (!taskQueue.isEmpty()){
            try {
                taskQueue.take().execute(accountService);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            synchronisationWorker.join(0,0);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        isRunning.set(false);
    }
}
