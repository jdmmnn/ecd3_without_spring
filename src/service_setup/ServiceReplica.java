package service_setup;

import ecd3.TransactionPropagation;

import java.util.ArrayList;

public class ServiceReplica implements Runnable {

    private static final ThreadLocal<ArrayList> threadLocalStorage = ThreadLocal.withInitial(ArrayList::new);

    @Override
    public void run() {
        ArrayList storage = threadLocalStorage.get();

        // Store some data
        storage.add("ThreadID", Thread.currentThread().getName());

        // Send a message to middleware
        TransactionPropagation middleware;
        Middleware.sendMessage("Hello from " + Thread.currentThread().getName());

        // Simulate some operations
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Check for messages in middleware
        String message;
        while ((message = Middleware.fetchMessage()) != null) {
            System.out.println(Thread.currentThread().getName() + " received: " + message);
        }

        // Output all data from the thread's storage
        System.out.println("Data in " + Thread.currentThread().getName() + "'s storage: " + storage.getAllData());
    }
}
