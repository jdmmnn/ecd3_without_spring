package ecd3;

import ecd3.domain.Aggregate;
import ecd3.domain.Operation;
import ecd3.domain.OperationEnum;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

public interface Transaction extends Serializable {

    void begin();

    void commit();

    void rollback(Long replicaId);

    void logOperation(String aggregateName, String aggregateId, OperationEnum operation, Object... parameters);

    Long getCommitTimeStamp();

    Set<Aggregate<?>> getWriteSet();

    Set<Aggregate<?>> getReadSet();

    List<Operation> getOperations();

    int getReplicaId();

    int getId();
}
