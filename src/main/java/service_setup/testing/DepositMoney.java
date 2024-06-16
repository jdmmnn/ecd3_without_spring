package service_setup.testing;

import service_setup.AccountService;
import service_setup.NoAccountFoundException;

public class DepositMoney implements Task {

    private final String name;
    private final double amount;

    public DepositMoney(String name, double amount) {
        this.name = name;
        this.amount = amount;
    }

    @Override
    public void execute(AccountService accountService) {
        try {
            accountService.depositMoney(name, amount);
        } catch (NoAccountFoundException e) {
            e.printStackTrace();
        }
    }
}
