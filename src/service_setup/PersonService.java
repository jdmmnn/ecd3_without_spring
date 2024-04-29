package service_setup;

import ecd3.Transaction;
import ecd3.TransactionManager;
import ecd3.TransactionManagerImpl;

public class PersonService {

    ThreadLocal<TransactionManager> transactionManager;

    ThreadLocal<PersonRepo> personRepo;

    public PersonService() {
        this.personRepo = ThreadLocalProvider.getPersonRepo();
        this.transactionManager = ThreadLocalProvider.getTransactionManager();
    }

    public Person addPerson(String name, int age) {
        Transaction transaction = transactionManager.get().bot();
        Person person = new Person(name, age);
        System.out.println("Person created: " + person.getId());
        personRepo.get().save(person);
        transactionManager.get().commit(transaction);
        return person;
    }

    public void updatePerson(String id, String name, int age) {
        Transaction transaction = transactionManager.get().bot();
        Person person = personRepo.get().findById(id);
        person.setName(name);
        person.setAge(age);
        personRepo.get().save(person);
        transactionManager.get().commit(transaction);
    }

    public void deletePerson(String id) {
        Transaction transaction = transactionManager.get().bot();
        Person person = personRepo.get().findById(id);
        personRepo.get().delete(person);
        transactionManager.get().commit(transaction);
    }

    public Person getPerson(String id) {
        return personRepo.get().findById(id);
    }
}
