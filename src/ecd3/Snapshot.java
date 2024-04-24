package ecd3;

import java.util.Set;

public interface Snapshot {
    Set<? extends Aggregate<?>> get();
}
