package service_setup;

import ecd3.TransactionManager;
import ecd3.TransactionManagerImpl;

import java.util.concurrent.atomic.AtomicInteger;

public class ThreadLocalProvider {

    private static final AtomicInteger transactionId = new AtomicInteger(0);

    private static final AtomicInteger uniqueId = new AtomicInteger(0);

    private static final ThreadLocal<Integer> replicaId = ThreadLocal.withInitial(uniqueId::getAndIncrement);
    private static final ThreadLocal<PersonRepo> personRepo = ThreadLocal.withInitial(PersonRepo::new);

    private static final ThreadLocal<PersonService> personService = ThreadLocal.withInitial(PersonService::new);

    private static final ThreadLocal<TransactionManager> transactionManager = ThreadLocal.withInitial(TransactionManagerImpl::new);

    public static ThreadLocal<PersonService> getPersonService() {
        return personService;
    }

    public static ThreadLocal<PersonRepo> getPersonRepo() {
        return personRepo;
    }

    public static ThreadLocal<TransactionManager> getTransactionManager() {
        System.err.println("ThreadLocalProvider.getTransactionManager");
        System.err.println("Thread.currentThread().getName(): " + Thread.currentThread().getName());
        return transactionManager;
    }

    public static AtomicInteger getTransactionId() {
        return transactionId;
    }

    public static int getReplicaId() {
        return replicaId.get();
    }
}
