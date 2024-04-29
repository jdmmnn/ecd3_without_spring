package ecd3;

import service_setup.Person;
import service_setup.ThreadLocalProvider;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public abstract class Repository<E extends Aggregate<?>, F extends Serializable> {

    public abstract Map<Version<F>, E> multiVersionPersistence();

    public abstract List<E> persistence();

    public abstract F getId(E aggregate);

    public void save(E aggregate) {
//        System.out.println("Saving aggregate: " + aggregate);
//        System.out.println("Aggregate ID: " + getId(aggregate));
//        System.out.println("Repository ID: " + System.identityHashCode(this));
//        System.out.println("Persistence ID: " + System.identityHashCode(persistence()));
        if (!persistence().contains(aggregate)) {
            persistence().add(aggregate);
        }
        int replicaId = ThreadLocalProvider.getReplicaId();
        multiVersionPersistence().put(new Version<F>(getId(aggregate), aggregate.getVersion()), CopyUtil.deepCopy(aggregate));
    }

    public E findById(F id) {
//        System.out.println("Finding aggregate by ID: " + id);
//        System.out.println("Repository ID: " + System.identityHashCode(this));
//        System.out.println("Persistence ID: " + System.identityHashCode(persistence()));
        E aggregate = persistence().stream().filter(e -> getId(e).equals(id)).findFirst().orElse(null);
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
//        System.out.println("Updating aggregate: " + aggregate);
//        System.out.println("Aggregate ID: " + getId(aggregate));
//        System.out.println("Repository ID: " + System.identityHashCode(this));
        aggregate.incrementVersion();
        multiVersionPersistence().put(new Version<F>(getId(aggregate), aggregate.getVersion()), CopyUtil.deepCopy(aggregate));
    }

    public void rollbackTo(E deepCopyOfOldVersionOfAggregate, int version) {
        deepCopyOfOldVersionOfAggregate.setVersion(version);
        persistence().add(deepCopyOfOldVersionOfAggregate);
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



