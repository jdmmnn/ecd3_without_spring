package service_setup;


import ecd3.Aggregate;
import ecd3.Repository;
import ecd3.RepositoryManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersonRepo extends Repository<Person, String> {

    private final Map<Version<String>, Person> multiVersionPersistence = new HashMap<>();
    private final List<Person> persistence = new ArrayList<>();

    @Override
    public Map<Version<String>, Person> multiVersionPersistence() {
        return multiVersionPersistence;
    }

    @Override
    public List<Person> persistence() {
        return persistence;
    }

    @Override
    public String getId(Person aggregate) {
        return aggregate.getId();
    }
}
