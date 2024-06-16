package service_setup.testing;

import ecd3.TransactionImpl;
import ecd3.domain.OperationEnum;
import ecd3.propa.MessageBuffer;
import java.time.Instant;
import service_setup.Account;
import service_setup.AccountService;

public class Transaction implements Task {

    Instant instant;

    public Transaction(Instant instant) {
        this.instant = instant;
    }

    @Override
    public void execute(AccountService accountService) {
        TransactionImpl transaction = new TransactionImpl(accountService.replicaId);
        transaction.setCommitTimeStamp(instant);
        transaction.logOperation("", "", OperationEnum.CREATE, new Account("test", 0));
        MessageBuffer.broadcast(accountService.replicaId, transaction);
    }
}
