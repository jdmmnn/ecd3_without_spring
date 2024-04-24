package service_setup;

import ecd3.RepositoryManager;
import ecd3.TransactionManager;
import ecd3.TransactionManagerImpl;

public class PersonService {

    TransactionManager transactionManager = TransactionManagerImpl.getInstance();

    PersonRepo personRepo = new PersonRepo();

    public void createPerson(String name, int age) {
        transactionManager.bot();
        Person person = new Person(name, age);
        personRepo.save(person);
        transactionManager.commit();
    }
}
