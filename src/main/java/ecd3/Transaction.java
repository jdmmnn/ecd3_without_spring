package ecd3;

import ecd3.domain.Aggregate;
import ecd3.domain.Operation;
import ecd3.domain.OperationEnum;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Transaction extends Serializable {

    void begin(Map<String, Integer> botVersions);

    void commit(Map<String, Integer> commitVersions);

    void rollback();

    void logOperation(String aggregateName, String aggregateId, OperationEnum operation, Object... parameters);

    void logWriteOperation(Aggregate<?> aggregate);

    Instant getCommitTimeStamp();

    Set<Aggregate<?>> getWriteSet();

    Set<Aggregate<?>> getReadSet();

    List<Operation> getOperations();

    Long getReplicaId();

    int getId();

    boolean isRollback();

    Snapshot getBotSnapShot();

    void setWasPropagated();

    boolean getIsEnd();

    Snapshot getCommitSnapshot();
}
