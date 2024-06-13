package service_setup.testing;

import service_setup.AccountService;

public class CreateAccount implements Task {

    String name;
    double balance;
    long simulatedDelay;
    
    public CreateAccount(String name, double balance, long simulatedDelay) {
        this.name = name;
        this.balance = balance;
        this.simulatedDelay = simulatedDelay;
    }

    @Override
    public void execute(AccountService accountService) {
        accountService.createAccount(name, balance);
        try {
            Thread.sleep(simulatedDelay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
