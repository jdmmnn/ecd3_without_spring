package service_setup;

import ecd3.domain.Aggregate;

import java.util.Objects;

public class Account extends Aggregate<Account> implements Comparable<Account> {

    // serializable
    private static final long serialVersionUID = 1L;

    private String name;
    private double balance;

    public Account(String name, double balance) {
        super();
        this.name = name;
        this.balance = balance;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getName() {
        return name;
    }

    public double getBalance() {
        return balance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Account account = (Account) o;
        return Objects.equals(name, account.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public String toString() {
        return "Account{" +
                "name='" + name + '\'' +
                ", balance=" + balance +
                '}';
    }

    @Override
    public int compareTo(Account o) {
        return this.name.compareTo(o.name);
    }
}
