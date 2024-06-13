package service_setup;


import ecd3.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentSkipListSet;

public class AccountRepo extends Repository<Account, String> {

    private final ConcurrentHashMap<Version<String>, Account> multiVersionPersistence;
    private final ConcurrentHashMap<String, Account> persistence;

    public AccountRepo() {
        multiVersionPersistence = new ConcurrentHashMap<>();
        persistence = new ConcurrentHashMap<>();
    }

    @Override
    public ConcurrentHashMap<Version<String>, Account> multiVersionPersistence() {
        return multiVersionPersistence;
    }

    @Override
    public ConcurrentHashMap<String, Account> persistence() {
        return persistence;
    }

    @Override
    public String getId(Account aggregate) {
        return aggregate.getId();
    }

    @Override
    public String getName(Account aggregate) {
        return aggregate.getName();
    }

    // find person by name
    public Optional<Account> findByName(String name) {
        return Optional.ofNullable(persistence().get(name));
    }
}
