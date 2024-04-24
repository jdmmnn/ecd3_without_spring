package ecd3;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public abstract class Repository<E extends Aggregate<?>, F extends Serializable> {

    public abstract Map<Version<F>, E> multiVersionPersistence();

    public abstract Map<F, E> persistence();

    public abstract F getId(E aggregate);

    public void save(E aggregate) {
        persistence().put(getId(aggregate), aggregate);
        aggregate.incrementVersion();
        multiVersionPersistence().put(new Version<F>(getId(aggregate), aggregate.getVersion()), CopyUtil.deepCopy(aggregate));
    }

    public E find(F id) {
        E aggregate = persistence().get(id);
        if (aggregate == null) {
            throw new NoSuchElementException();
        }
        return aggregate;
    }

    public E findSpecificVersion(F id, int version) {
        E aggregate = multiVersionPersistence().get(new Version<F>(id, version));
        if (aggregate == null) {
            throw new NoSuchElementException();
        }
        return aggregate;
    }

    public void delete(E aggregate) {
        persistence().remove(getId(aggregate));
        multiVersionPersistence().remove(new Version<F>(getId(aggregate), aggregate.getVersion()));
    }

    public void update(E aggregate) {
        aggregate.incrementVersion();
        multiVersionPersistence().put(new Version<F>(getId(aggregate), aggregate.getVersion()), CopyUtil.deepCopy(aggregate));
    }

    public void rollbackTo(E deepCopyOfOldVersionOfAggregate, int version) {
        deepCopyOfOldVersionOfAggregate.setVersion(version);
        persistence().put(getId(deepCopyOfOldVersionOfAggregate), deepCopyOfOldVersionOfAggregate);
        multiVersionPersistence().keySet().removeIf(key -> key.getAggregateId().equals(getId(deepCopyOfOldVersionOfAggregate)) && key.getVersion() > version);
    }


    public static class Version<F> {
        private final F aggregateId;
        private final int version;

        public Version(F aggregateId, int version) {
            this.aggregateId = aggregateId;
            this.version = version;
        }

        public F getAggregateId() {
            return aggregateId;
        }

        public int getVersion() {
            return version;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Version<?> version1 = (Version<?>) o;
            return version == version1.version && aggregateId.equals(version1.aggregateId);
        }

        @Override
        public int hashCode() {
            int result = aggregateId.hashCode();
            result = 31 * result + version;
            return result;
        }
    }
}



