import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.in;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import service_setup.ThreadLocalProvider;
import service_setup.testing.CreateAccount;
import service_setup.Account;
import service_setup.ServiceReplica;
import service_setup.testing.DepositMoney;
import service_setup.testing.End;
import service_setup.testing.Sleep;
import service_setup.testing.Task;
import service_setup.testing.Transaction;
import service_setup.testing.WithdrawMoney;

class BasicFunctionalityTest {


    void testSetup(List<ServiceReplica> serviceReplicas, List<Account> expectedResults) {
        serviceReplicas.forEach(Thread::start);


    }


    @Test
    void runBenchmark() {
        // Define the number of persons
        int numOfAccounts = 100;

        // Run the benchmarks
        for (int numOfReplicas = 1; numOfReplicas <= 100; numOfReplicas++) {
            long benchmark = benchmark(numOfReplicas, numOfAccounts);

            // Create a new list with the number of replicas, the number of persons, and the benchmark result
            List<String> benchmarkResult = new ArrayList<>();
            benchmarkResult.add(String.valueOf(numOfReplicas));
            benchmarkResult.add(String.valueOf(numOfAccounts));
            benchmarkResult.add(String.valueOf(benchmark));

            // Write the benchmark result to the CSV file
            writeCsvFile(List.of(benchmarkResult), "benchmark-results.csv", true);
        }
    }

    @ParameterizedTest
    @MethodSource("provideParametersForBenchmark")
    void runIsolatedBenchmark(int numOfReplicas, int numOfAccounts) {
        long benchmark = benchmark(numOfReplicas, numOfAccounts);
        System.out.println("Benchmark took " + benchmark + " seconds");
    }

    private static Stream<Arguments> provideParametersForBenchmark() {
        return Stream.of(
                Arguments.of(1, 10),
                Arguments.of(2, 10),
                Arguments.of(3, 10),
                Arguments.of(4, 10),
                Arguments.of(5, 10),
                Arguments.of(6, 10),
                Arguments.of(7, 10),
                Arguments.of(8, 10),
                Arguments.of(9, 10),
                Arguments.of(10, 10)
//                Arguments.of(5, 10),
//                Arguments.of(5, 20),
//                Arguments.of(5, 30),
//                Arguments.of(10, 10),
//                Arguments.of(10, 20),
//                Arguments.of(10, 30),
//                Arguments.of(15, 10),
//                Arguments.of(15, 20),
//                Arguments.of(15, 30)
                // add more combinations if needed
                        );
    }

    // run one isolated benchnark with 5 replicas and 100 persons
    @Test
    void runIsolatedBenchmark1() {
        long benchmark = benchmark(9, 100);
        System.out.println("Benchmark took " + benchmark + " seconds");
    }

    @Test
    void runAccountOperationsTest() {
        // Define the number of accounts, operations, and replicas
        int numOfAccounts = 2;
        int numOfOperations = 10;
        int numOfReplicas = 3;

        // Create a list of service replicas
        List<ServiceReplica> serviceReplicas = IntStream.range(0, numOfReplicas).mapToObj(i -> {
            // Create a task queue
            BlockingQueue<Task> taskQueue = new LinkedBlockingQueue<>();

            // Create accounts and add deposit and withdraw tasks to the queue
            for (int j = 0; j < numOfAccounts; j++) {
                Account account = getAccount(i, j, 0);
                taskQueue.add(new CreateAccount(account.getName(), account.getBalance(), getSimulatedDelay()));
                taskQueue.add(new Sleep(10));
                for (int k = 0; k < numOfOperations; k++) {
                    taskQueue.add(new DepositMoney(account.getName(), 100));
                    taskQueue.add(new Sleep(10));
                    taskQueue.add(new WithdrawMoney(account.getName(), 50));
                }
            }
            taskQueue.add(new End());

            // Create a service replica with the task queue
            ServiceReplica serviceReplica = new ServiceReplica(String.valueOf(i), taskQueue);
//            for (int i1 = 0; i1 < numOfAccounts; i1++) {
//                serviceReplica.accountRepo.save(getAccount(serviceReplica.replicaId, i1, 0));
//            }
            return serviceReplica;
        }).toList();

        // Start the service replicas
        serviceReplicas.forEach(Thread::start);

        // Wait for the service replicas to finish
        while (serviceReplicas.stream().anyMatch(ServiceReplica::isRunning)) {
            Thread.onSpinWait();
        }

        // Assert that all accounts have the expected balance
        for (ServiceReplica serviceReplica : serviceReplicas) {
            for (int i = 0; i < numOfAccounts; i++) {
                serviceReplica.accountService.findPerson(i + "john" + i).ifPresent(account -> {
                    assertEquals(500, account.getBalance(), "Account balance is incorrect");
                });
            }
        }
    }

    @Test
    void onlyWithdrawls() {
        int numOfAccounts = 10;
        int numOfOperations = 100;

        List<ServiceReplica> serviceReplicaStream = IntStream.range(0, 2).mapToObj(e -> {
            BlockingQueue<Task> taskQueue = new LinkedBlockingDeque<>();

            for (int i1 = 0; i1 < numOfAccounts; i1++) {
                Account account = getAccount(e, i1, 10000);
                taskQueue.add(new CreateAccount(account));
                taskQueue.add(new Sleep(100));
                for (int i = 0; i < numOfOperations; i++) {
                    taskQueue.add(new WithdrawMoney(account.getName(), 100));
                }
            }
            taskQueue.add(new End());

            return new ServiceReplica(String.valueOf(e), taskQueue);
        }).toList();

        serviceReplicaStream.forEach(Thread::start);

        while (serviceReplicaStream.stream().anyMatch(ServiceReplica::isRunning)) {
            Thread.onSpinWait();
        }

        serviceReplicaStream.forEach(i -> {
            i.accountRepo.persistence().values().forEach(e -> assertThat(e, BalanceMatcher.hasBalance(0L)));
        });
    }

    @Test
    void rollback() {
        BlockingQueue<Task> taskBlockingQueue = new LinkedBlockingQueue<>();

        taskBlockingQueue.add(new Transaction(Instant.now()));
        taskBlockingQueue.add(new Transaction(Instant.now().minus(1000, ChronoUnit.MILLIS)));


        ServiceReplica serviceReplica = new ServiceReplica("1", taskBlockingQueue);
        serviceReplica.start();

        while (serviceReplica.isRunning()) {
            Thread.onSpinWait();
        }
    }

    @Test
    void runConcurrentWithdrawalTest() {
        // Define the number of replicas
        int numOfReplicas = 2;

        // Create a list of service replicas
        List<ServiceReplica> serviceReplicas = IntStream.range(0, numOfReplicas).mapToObj(i -> {
            // Create a task queue
            BlockingQueue<Task> taskQueue = new LinkedBlockingQueue<>();

            // Create an account and add withdrawal tasks to the queue
            Account account = getAccount(0, 0, 50);
            if (i == 0) {
                taskQueue.add(new CreateAccount("0john0", 50, getSimulatedDelay()));
            }
            taskQueue.add(new Sleep(100));
            taskQueue.add(new WithdrawMoney("0john0", 50));

            // Create a service replica with the task queue
            return new ServiceReplica(String.valueOf(i), taskQueue);
        }).toList();

        // Start the service replicas
        serviceReplicas.forEach(Thread::start);

        // Wait for the service replicas to finish
        while (serviceReplicas.stream().anyMatch(ServiceReplica::isRunning)) {
            Thread.onSpinWait();
        }

        // Assert that the account has the expected balance
        for (ServiceReplica serviceReplica : serviceReplicas) {
            serviceReplica.accountService.findPerson("0john0").ifPresent(account -> {
                assertEquals(0, account.getBalance(), "Account balance is incorrect");
            });
        }
    }

    private long benchmark(int numOfReplicas, int numOfAccounts) {
        // print the number of replicas and persons in one staement as start of benchmark run
        System.out.println("Benchmarking " + numOfReplicas + " replicas with " + numOfAccounts + " accounts");
        System.gc();
        Instant start = Instant.now();
        List<ServiceReplica> serviceReplicas = IntStream.range(0, numOfReplicas).mapToObj(i -> {
            BlockingQueue<Task> taskQueue = new LinkedBlockingQueue<>();
            taskQueue.add(new Sleep(100));
            fillTaskQueue(taskQueue, i, numOfReplicas, numOfAccounts);
            return new ServiceReplica(String.valueOf(i + 1), taskQueue);
        }).toList();

        // start the service replicas
        serviceReplicas.forEach(Thread::start);

        // wait for the service replicas to finish
        serviceReplicas.forEach(e -> {
            try {
                e.join();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        });
        Instant end = Instant.now();

        List<Account> actual = IntStream.range(0, numOfReplicas)
                .mapToObj(e -> IntStream.range(0, numOfAccounts).mapToObj(i -> getAccount(e, i, 0)).toList())
                .flatMap(
                        Collection::stream).sorted().toList();

        // assert that all serviceReplicas contain all the same persons in any order at the end of execution
        serviceReplicas.forEach(e -> {
            List<Account> list = e.accountService.accountRepo.persistence().values().stream().sorted().toList();

            for (Account account : actual) {
                assertThat(list, hasItem(account));
            }
            list.forEach(i -> {
                String name = i.getName();
                Account actualAccount = actual.stream().filter(l -> l.getName().equals(name)).findFirst().orElseThrow();
                assertThat(i, BalanceMatcher.hasBalance(actualAccount.getBalance()));
            });
        });

        List<List<String>> csvData = ThreadLocalProvider.getLog().stream().map(e ->
                        List.of(
                                String.valueOf(e.workerReplicaId()),
                                String.valueOf(e.producerReplicaId()),
                                String.valueOf(e.transactionId()),
                                String.valueOf(e.aggregateId().toString()),
                                String.valueOf(e.operation().toString()),
                                String.valueOf(e.instant().getEpochSecond()) + "." + String.valueOf(e.instant().getNano()))
                                                                              ).collect(Collectors.toList());
        List<String> csvHeader = new ArrayList<>();
        csvHeader.add("workerReplicaId,producerReplicaId,transactionId,aggregateId,operation,instant");
        csvData.addFirst(csvHeader);

        writeCsvFile(csvData, "log-" + numOfReplicas + "-" + numOfAccounts + ".csv", false);
        return end.toEpochMilli() - start.toEpochMilli();
    }

    private static void fillTaskQueue(BlockingQueue<Task> taskQueue, int replicaNumber, int numOfReplicas, int numOfPersons) {
        for (int i = 0; i < numOfPersons; i++) {
            taskQueue.add(new CreateAccount(
                    getAccount(replicaNumber, i, 0).getName(), getAccount(replicaNumber, i, 0).getBalance(), getSimulatedDelay()
            ));
        }
        int i = numOfReplicas * numOfPersons;
        for (int i1 = 0; i1 < i; i1++) {
            taskQueue.add(new Sleep(10));
        }
        taskQueue.add(new End());
    }

    private static Account getAccount(long replicaNumber, int accountNumber, int balance) {
        return new Account(replicaNumber + "john" + accountNumber, balance);
    }

    private static int getSimulatedDelay() {
        //return 0;
        return 10;
    }

    private void writeCsvFile(List<List<String>> csvData, String path, boolean append) {
        // write the csvData to a file at the path
        try (PrintWriter writer = new PrintWriter(new FileWriter(path, append))) {
            for (List<String> rowData : csvData) {
                writer.println(String.join(", ", rowData));
            }
        } catch (IOException e) {
            System.out.println("An error occurred while writing to the CSV file: " + e.getMessage());
        }
    }

    public static class BalanceMatcher extends TypeSafeMatcher<Account> {

        private final double expectedBalance;

        public BalanceMatcher(double expectedBalance) {
            this.expectedBalance = expectedBalance;
        }

        @Override
        protected boolean matchesSafely(Account item) {
            return item.getBalance() == expectedBalance;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("a account with balance ").appendValue(expectedBalance);
        }

        @Override
        protected void describeMismatchSafely(Account item, Description mismatchDescription) {
            super.describeMismatchSafely(item, mismatchDescription);
        }

        public static BalanceMatcher hasBalance(double expectedBalance) {
            return new BalanceMatcher(expectedBalance);
        }
    }
}
