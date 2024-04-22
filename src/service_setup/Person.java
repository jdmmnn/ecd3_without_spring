package service_setup;

import ecd3.Aggregate;
import ecd3.Operation;
import ecd3.TransactionManager;

import java.util.UUID;

public class Person extends Aggregate {
    private String id;
    private String name;
    private int age;

    public Person(String name, int age) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.age = age;
    }

    public void setName(String name) {
        logOperation(new Operation(this.getClass(), "setName", new Object[]{name}));
        this.name = name;
    }

    public void setAge(int age) {
        logOperation(null, new Operation(Person.class, "setAge", new Object[]{age}));
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }
}
