package service_setup;

import ecd3.propa.MessageBuffer;

public class ServiceReplica extends Thread {

    ThreadLocal<PersonService> personService = ThreadLocalProvider.getPersonService();

    @Override
    public void run() {
        MessageBuffer.registerThread(Thread.currentThread());
        // service replica was started log
        System.out.printf("Service replica thread: %s was started%n", Thread.currentThread().getName());
        int counter = 0;
        while (true) {
            try {
                // do some domain logic
                Person john = personService.get().addPerson(Thread.currentThread().getName() + "john" + counter++, 25);
                while (!MessageBuffer.getBuffer(Thread.currentThread()).isEmpty()) {
                    ThreadLocalProvider.getTransactionManager().get().consumeBuffer();
                }
                Thread.sleep(10000);
                //personService.get().updatePerson(john.getId(), "John Doe", 26);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
