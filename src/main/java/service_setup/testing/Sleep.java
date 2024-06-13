package service_setup.testing;

import static java.lang.Thread.sleep;

import service_setup.AccountService;

public class Sleep implements Task {

    int sleep;

    public Sleep(int sleep) {
        this.sleep = sleep;
    }

    @Override
    public void execute(AccountService accountService) {
        try {
            sleep(sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
