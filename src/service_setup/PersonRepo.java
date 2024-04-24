package service_setup;


import ecd3.Aggregate;
import ecd3.Repository;
import ecd3.RepositoryManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersonRepo extends Repository<Person, String> {

    {
        RepositoryManager.getInstance().<Person, String>registerRepository(Person.class, this);
    }

    @Override
    public Map<Version<String>, Person> multiVersionPersistence() {
        return new HashMap<>();
    }

    @Override
    public Map<String, Person> persistence() {
        return new HashMap<>();
    }

    @Override
    public String getId(Person aggregate) {
        return aggregate.getId();
    }
}
