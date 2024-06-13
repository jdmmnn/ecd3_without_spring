package ecd3.util;

import java.io.*;

public class CopyUtil {
    public static  <T> T deepCopy(T object) {
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
