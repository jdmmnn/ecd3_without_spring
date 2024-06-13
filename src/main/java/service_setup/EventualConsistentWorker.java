package service_setup;

import ecd3.EventualConsistentService;

public class EventualConsistentWorker extends Thread {

    public final long replicaId;
    public final ServiceReplica replicaThread;
    public final AccountRepo accountRepo;
    EventualConsistentService eventualConsistentService;

    public EventualConsistentWorker(
            String name,
            long replicaId) {
        super(name);
        this.replicaId = replicaId;
        this.accountRepo = ThreadLocalProvider.getAccountRepo(replicaId);
        this.eventualConsistentService = ThreadLocalProvider.getEventualConsistentService(replicaId);
        this.replicaThread = ThreadLocalProvider.getReplicaThread(replicaId);
    }

    @Override
    public void run() {
        while (replicaThread.isRunning()){
            //eventualConsistentService.evaluateTransactionTail();
        }
    }
}
