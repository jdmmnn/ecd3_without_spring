package service_setup.testing;

import service_setup.Account;
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

    public CreateAccount(Account account) {
        this.name = account.getName();
        this.balance = account.getBalance();
    }

    @Override
    public void execute(AccountService accountService) {
        accountService.createAccount(name, balance);
//        accountService.accountRepo.save(new Account(name, balance));
        try {
            Thread.sleep(simulatedDelay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
