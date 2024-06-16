package service_setup.testing;

import ecd3.SnapshotImpl;
import ecd3.TransactionImpl;
import ecd3.propa.MessageBuffer;
import java.util.Map;
import service_setup.AccountService;
import service_setup.ThreadLocalProvider;

public class End implements Task {

    @Override
    public void execute(AccountService accountService) {
        Long replicaId = ThreadLocalProvider.getReplicaIdByThread(Thread.currentThread());
        TransactionImpl transaction = new TransactionImpl(replicaId);
        transaction.isEnd = true;
        transaction.botSnapshot = new SnapshotImpl(null);
        transaction.commit(Map.of());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        MessageBuffer.broadcast(replicaId, transaction);
    }
}
