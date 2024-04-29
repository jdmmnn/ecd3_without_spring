package service_setup;

import ecd3.Aggregate;
import ecd3.Operation;
import ecd3.TransactionManager;

import java.util.Objects;
import java.util.UUID;

public class Person extends Aggregate<Person> {

    // serializable
    private static final long serialVersionUID = 1L;

    private String name;
    private int age;
    private int replicaId;

    public Person(String name, int age) {
        super();
        this.name = name;
        this.age = age;
        this.replicaId = ThreadLocalProvider.getReplicaId();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Person person = (Person) o;
        return age == person.age && Objects.equals(name, person.name);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(name);
        result = 31 * result + age;
        return result;
    }
}
