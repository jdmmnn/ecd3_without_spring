package service_setup.testing;

import service_setup.AccountService;
import service_setup.InsufficientFundsException;
import service_setup.NoAccountFoundException;

public class WithdrawMoney implements Task {

    private final String name;
    private final double amount;

    public WithdrawMoney(String name, double amount) {
        this.name = name;
        this.amount = amount;
    }

    @Override
    public void execute(AccountService accountService) {
        try {
            Thread.sleep(100);
            accountService.withdrawMoney(name, amount);
        } catch (InsufficientFundsException | NoAccountFoundException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
