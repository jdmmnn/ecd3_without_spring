package ecd3;

import ecd3.domain.Aggregate;
import java.io.Serializable;
import java.util.Set;

public interface Snapshot extends Serializable {
    Set<? extends Aggregate<?>> get();
}
