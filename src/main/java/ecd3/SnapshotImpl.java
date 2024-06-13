package ecd3;

import ecd3.domain.Aggregate;
import ecd3.util.CopyUtil;
import java.util.Set;
import java.util.stream.Collectors;

public class SnapshotImpl implements Snapshot {

    private final Set<? extends Aggregate<?>> snapshot;

    public SnapshotImpl(Set<? extends Aggregate<?>> objects) {
        snapshot = objects.stream().map(CopyUtil::deepCopy).collect(Collectors.toUnmodifiableSet());
    }

    public Set<? extends Aggregate<?>> get() {
        return snapshot;
    }
}
