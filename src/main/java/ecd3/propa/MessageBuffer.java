package ecd3.propa;

import ecd3.Transaction;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MessageBuffer {

    static Map<Long, ConcurrentLinkedQueue<Transaction>> buffers = new HashMap<>();

    public static synchronized void registerThread(Thread thread) {

    }

    public static synchronized ConcurrentLinkedQueue<Transaction> registerOrGet(Long replicaId) {
        if (buffers.containsKey(replicaId)) {
            return buffers.get(replicaId);
        }
        ConcurrentLinkedQueue<Transaction> queue = new ConcurrentLinkedQueue<>();
        buffers.put(replicaId, queue);
        return queue;
    }

    public static synchronized void broadcast(Long replicaId, Transaction transaction) {
        buffers.forEach((t, buffer) -> {
            if (t != replicaId) {
                buffer.add(transaction);
            }
        });
    }
}
