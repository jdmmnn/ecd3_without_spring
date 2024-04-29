package ecd3;

import java.io.Serializable;
import java.util.Set;

public interface Snapshot extends Serializable {
    Set<? extends Aggregate<?>> get();
}
