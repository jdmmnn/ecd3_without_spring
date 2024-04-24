package service_setup;

import ecd3.Aggregate;
import ecd3.Operation;
import ecd3.TransactionManager;

import java.util.UUID;

public class Person extends Aggregate<Person> {
    private String id;
    private String name;
    private int age;

    public Person(String name, int age) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.age = age;
        super.logWriteOperation();
        super.logOperation(this);
    }



    public void setName(String name) {
        this.name = name;
        super.logWriteOperation();
        super.logOperation(this);
    }

    public void setAge(int age) {
        this.age = age;
        super.logWriteOperation();
        super.logOperation(this);
    }

    public String getName() {
        super.logReadOperation();
        return name;
    }

    public int getAge() {
        super.logReadOperation();
        return age;
    }
}
