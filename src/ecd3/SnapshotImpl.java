package ecd3;

import java.io.*;
import java.util.Set;
import java.util.stream.Collectors;

public class SnapshotImpl implements Snapshot {

    private final Set<? extends Aggregate> snapshot;

    public SnapshotImpl(Set<? extends Aggregate> objects) {
        snapshot = objects.stream().map(SnapshotImpl::deepCopy).collect(Collectors.toUnmodifiableSet());
    }

    public Set<? extends Aggregate> get() {
        return snapshot;
    }

    private static  <T> T deepCopy(T object) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(outputStream);
            out.writeObject(object);
            out.flush();
            out.close();

            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            ObjectInputStream in = new ObjectInputStream(inputStream);
            @SuppressWarnings("unchecked")
            T deepCopiedObject = (T) in.readObject();
            in.close();

            return deepCopiedObject;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
