package ecd3;

import java.util.Map;

public class SnapshotImpl implements Snapshot {

    private final Map<String, Integer> snapshot;

    public SnapshotImpl(Map<String, Integer> snapshot) {
        this.snapshot = snapshot;
    }

    public Map<String, Integer> get() {
        if (snapshot == null) {
            return Map.of();
        }
        return snapshot;
    }
}
