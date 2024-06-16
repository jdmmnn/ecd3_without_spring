package ecd3;

import ecd3.domain.Aggregate;
import ecd3.util.CopyUtil;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import service_setup.AccountAllReadyExistsException;
import service_setup.NoAccountFoundException;

import java.io.Serializable;
import java.util.Map;
import java.util.NoSuchElementException;
import service_setup.ThreadLocalProvider;

public abstract class Repository<E extends Aggregate<?>, F extends Serializable> {

    public abstract ConcurrentHashMap<Version, E> multiVersionPersistence();

    public abstract ConcurrentHashMap<String, E> persistence();

    public abstract String getId(E aggregate);

    public abstract String getName(E aggregate);

    public void save(E aggregate) throws AccountAllReadyExistsException {
        if (persistence().containsKey(getName(aggregate))) {
            Long replicaIdByThread = ThreadLocalProvider.getReplicaIdByThread(Thread.currentThread());
            throw new AccountAllReadyExistsException("Account: " + getName(aggregate) + " already exists in replica " + replicaIdByThread);
        }
        persistence().put(getName(aggregate), CopyUtil.deepCopy(aggregate));
        multiVersionPersistence().put(new Version(getId(aggregate), aggregate.getVersion()), CopyUtil.deepCopy(aggregate));
    }

    public void delete(E aggregate) {
        persistence().remove(getName(aggregate));
        multiVersionPersistence().remove(new Version(getId(aggregate), aggregate.getVersion()));
    }

    public void update(E aggregate) throws NoAccountFoundException {
        if (!persistence().containsKey(getName(aggregate))) {
            throw new NoAccountFoundException("No Account with name: " + getName(aggregate) + " found");
        }
        aggregate.incrementVersion();
        persistence().put(getName(aggregate), CopyUtil.deepCopy(aggregate));
        multiVersionPersistence().put(new Version(getId(aggregate), aggregate.getVersion()), CopyUtil.deepCopy(aggregate));
    }

    public E findById(String id) {
        return persistence().values()
                .stream()
                .filter(aggregate1 -> getId(aggregate1).equals(id))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
    }

    public Map<String, Integer> getSnapshotVersions() {
        return persistence().values().stream().collect(Collectors.toMap(e -> e.getId(), e -> e.getVersion()));
    }

    public E findSpecificVersion(String id, int version) {
        E aggregate = multiVersionPersistence().get(new Version(id, version));
        if (aggregate == null) {
            throw new NoSuchElementException();
        }
        return aggregate;
    }

    public void rollbackTo(String id, int version) {
        E e = multiVersionPersistence().get(new Version(id, version));
        if (e == null) {
            return;
        }
        persistence().put(getName(e), CopyUtil.deepCopy(e));
        multiVersionPersistence().keySet()
                .removeIf(key -> key.getAggregateId().equals(id) && key.getVersion() > version);
    }

    public void rollbackToTransaction(Transaction first) {
        Map<String, Integer> versionMap = first.getBotSnapShot().get();
        versionMap.keySet().forEach(id -> rollbackTo(id, versionMap.get(id)));

        first.getWriteSet().forEach(e -> {
            if (persistence().containsKey(getName((E) e))) {
                if (e.getVersion() == 0) {
                    delete((E) e);
                } else {
                    Integer versionToRollbackTo = first.getBotSnapShot().get().get(e.getId());
                    if (versionToRollbackTo != null) {
                        rollbackTo(((E) e).getId(), versionToRollbackTo);
                    }
                }
            }
        });
    }

    public static class Version implements Comparable<Version> {

        private final String aggregateId;
        private final int version;

        public Version(String aggregateId, int version) {
            this.aggregateId = aggregateId;
            this.version = version;
        }

        public String getAggregateId() {
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

            Version version1 = (Version) o;
            return version == version1.version && aggregateId.equals(version1.aggregateId);
        }

        @Override
        public int hashCode() {
            int result = aggregateId.hashCode();
            result = 31 * result + version;
            return result;
        }

        @Override
        public int compareTo(Version o) {
            int idCompare = getAggregateId().compareTo(o.getAggregateId());
            if (idCompare != 0) {
                return idCompare;
            }
            return Long.compare(this.getVersion(), o.getVersion());
        }
    }
}



