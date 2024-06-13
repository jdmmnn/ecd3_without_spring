package service_setup;

import ecd3.EventualConsistentService;
import ecd3.Transaction;
import ecd3.TransactionManager;
import ecd3.TransactionManagerImpl;

import ecd3.domain.OperationEnum;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadLocalProvider {

    private static final boolean benchmark = true;
    public static final boolean randomExceptions = true;

    private static final AtomicInteger transactionId = new AtomicInteger(0);

    private static final AtomicInteger uniqueId = new AtomicInteger(0);

    private static final Map<Long, AccountRepo> personRepos = new ConcurrentHashMap<>();
    private static final Map<Long, AccountService> personServices = new ConcurrentHashMap<>();
    private static final Map<Long, TransactionManager> transactionManagers = new ConcurrentHashMap<>();
    private static final Map<Long, ConcurrentLinkedDeque<Transaction>> transactionLogs = new ConcurrentHashMap<>();
    private static final Map<Long, ConcurrentSkipListSet<Transaction>> transactionTails = new ConcurrentHashMap<>();
    private static final Map<Long, ServiceReplica> replicaThreads = new ConcurrentHashMap<>();
    private static final Map<Long, EventualConsistentService> eventualConsistentServices = new ConcurrentHashMap<>();

    private static final ThreadLocal<Integer> replicaId = ThreadLocal.withInitial(uniqueId::getAndIncrement);

    public static Collection<LogEntry> getLog() {
        if (benchmark) {
            return log;
        }
        return null;
    }

    public static ConcurrentSkipListSet<Transaction> getTransactionTail(Long replicaId) {
        return transactionTails.computeIfAbsent(replicaId, k -> new ConcurrentSkipListSet<>(Comparator.comparingLong(Transaction::getCommitTimeStamp)));
    }

    public record LogEntry(long workerReplicaId, int producerReplicaId, String aggregateId, int transactionId, OperationEnum operation,
                           Instant instant) {}

    private static final ConcurrentLinkedDeque<LogEntry> log = new ConcurrentLinkedDeque<>();

    public static AccountService getPersonService(Long replicaId) {
        return personServices.computeIfAbsent(replicaId, AccountService::new);
    }

    public static AccountRepo getAccountRepo(Long replicaId) {
        return personRepos.computeIfAbsent(replicaId, k -> new AccountRepo());
    }

    public static TransactionManager getTransactionManager(Long replicaId) {
        return transactionManagers.computeIfAbsent(replicaId, TransactionManagerImpl::new);
    }

    public static ConcurrentLinkedDeque<Transaction> getTransactionLog(Long threadId) {
        return transactionLogs.computeIfAbsent(threadId, k -> new ConcurrentLinkedDeque<Transaction>());
    }

    public static EventualConsistentService getEventualConsistentService(Long replicaId) {
        return eventualConsistentServices.computeIfAbsent(replicaId, EventualConsistentService::new);
    }

    public static void registerReplicaThread(Long replicaId, ServiceReplica thread) {
        replicaThreads.put(replicaId, thread);
    }

    public static ServiceReplica getReplicaThread(Long replicaId) {
        return replicaThreads.get(replicaId);
    }

    public static AtomicInteger getTransactionId() {
        return transactionId;
    }

    public static int getReplicaId() {
        return replicaId.get();
    }

    public static void log(
            long workerReplicaId, int producerReplicaId, String aggregateId, int transactionId, OperationEnum operation, Instant instant) {
        if (benchmark) {
            log.add(new LogEntry(workerReplicaId, producerReplicaId, aggregateId, transactionId, operation, instant));
        }
    }

}
