package ecd3;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class RepositoryManager {
    /*private static RepositoryManager instance;
    private final Map<Class<? extends Aggregate<?>>, Repository<?, ?>> repositories = new HashMap<>();

    private RepositoryManager() {}

    public static synchronized RepositoryManager getInstance() {
        if (instance == null) {
            instance = new RepositoryManager();
        }
        return instance;
    }

    public <A extends Aggregate<A>, I extends Serializable> void registerRepository(Class<A> aggregateClass, Repository<A, I> repository) {
        repositories.put(aggregateClass, repository);
    }

    @SuppressWarnings("unchecked")
    public <A extends Aggregate<A>, I extends Serializable> Repository<A, I> getRepository(Class<A> aggregateClass) {
        return (Repository<A, I>) repositories.get(aggregateClass);
    }*/
}
