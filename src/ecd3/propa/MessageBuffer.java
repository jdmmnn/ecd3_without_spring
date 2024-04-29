package ecd3.propa;

import ecd3.Transaction;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MessageBuffer {

    static Map<Thread, ConcurrentLinkedQueue<Transaction>> buffers = new HashMap<>();

    public static synchronized void registerThread(Thread thread) {
        buffers.put(thread, new ConcurrentLinkedQueue<>());
    }

    public static synchronized ConcurrentLinkedQueue<Transaction> getBuffer(Thread thread) {
        return buffers.get(thread);
    }

    public static synchronized void broadcast(Thread thread, Transaction transaction) {
        System.err.println("Broadcasting transaction: " + transaction);
        buffers.forEach((t, buffer) -> {
            if (!t.equals(thread)) {
                buffer.add(transaction);
            }
        });
    }
}
