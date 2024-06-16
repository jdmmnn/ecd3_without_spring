package ecd3;

import java.io.Serializable;
import java.util.Map;

public interface Snapshot extends Serializable {
    Map<String, Integer> get();
}
