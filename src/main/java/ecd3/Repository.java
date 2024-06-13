package ecd3;

import ecd3.domain.Aggregate;
import ecd3.util.CopyUtil;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentSkipListSet;
import service_setup.ThreadLocalProvider;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public abstract class Repository<E extends Aggregate<?>, F extends Serializable> {

    public abstract ConcurrentHashMap<Version<F>, E> multiVersionPersistence();

    public abstract ConcurrentHashMap<F, E> persistence();

    public abstract F getId(E aggregate);

    public abstract F getName(E aggregate);

    public void save(E aggregate) {
        persistence().put(getName(aggregate), CopyUtil.deepCopy(aggregate));
        multiVersionPersistence().put(new Version<F>(getId(aggregate), aggregate.getVersion()), CopyUtil.deepCopy(aggregate));
    }

    public E findById(F id) {
        return persistence().values()
                .stream()
                .filter(aggregate1 -> getId(aggregate1).equals(id))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
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
        persistence().put(getName(aggregate), CopyUtil.deepCopy(aggregate));
        aggregate.incrementVersion();
        multiVersionPersistence().put(new Version<F>(getId(aggregate), aggregate.getVersion()), CopyUtil.deepCopy(aggregate));
    }

    public void rollbackTo(E deepCopyOfOldVersionOfAggregate, int version) {
        deepCopyOfOldVersionOfAggregate.setVersion(version);
        persistence().put(getName(deepCopyOfOldVersionOfAggregate), deepCopyOfOldVersionOfAggregate);
        multiVersionPersistence().keySet()
                .removeIf(key -> key.getAggregateId().equals(getId(deepCopyOfOldVersionOfAggregate)) && key.getVersion() > version);
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
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

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



