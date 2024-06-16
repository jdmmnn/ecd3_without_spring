package ecd3.propa;

import ecd3.Transaction;

import ecd3.util.CopyUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class MessageBuffer {

    static Map<Long, BlockingDeque<Transaction>> buffers = new HashMap<>();

    public static synchronized BlockingDeque<Transaction> registerOrGet(Long replicaId) {
        if (buffers.containsKey(replicaId)) {
            return buffers.get(replicaId);
        }
        BlockingDeque<Transaction> queue = new LinkedBlockingDeque<>();
        buffers.put(replicaId, queue);
        return queue;
    }

    public static synchronized void broadcast(Long replicaId, Transaction transaction) {
        buffers.forEach((t, buffer) -> {
//            if (t != replicaId) {
            buffer.add(CopyUtil.deepCopy(transaction));
//            }
        });
    }
}
