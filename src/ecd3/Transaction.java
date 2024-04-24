package ecd3;

import java.util.Set;

public interface Transaction {

    void begin();

    void commit();

    void rollback();

    void logUpdateOperation(String aggregateName, String aggregateId, Object... parameters);

    void logReadOperation(Aggregate<?> aggregate);

    void logWriteOperation(Aggregate<?> aggregate);

    Long getCommitTimeStamp();

    Snapshot getCommitSnapshot();

    Set<Aggregate<?>> getWriteSet();

    Set<Aggregate<?>> getReadSet();
}
