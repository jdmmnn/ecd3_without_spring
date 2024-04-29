package ecd3;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public interface Transaction extends Serializable {

    void begin();

    void commit();

    void rollback();

    void logUpdateOperation(String aggregateName, String aggregateId, Object... parameters);

    void logReadOperation(Aggregate<?> aggregate);

    void logWriteOperation(Aggregate<?> aggregate);

    Long getCommitTimeStamp();

    Set<Aggregate<?>> getWriteSet();

    Set<Aggregate<?>> getReadSet();

    List<Operation> getOperations();

    int getReplicaId();
}
