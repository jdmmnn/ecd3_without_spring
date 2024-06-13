import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.in;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import service_setup.AccountRepo;
import service_setup.ThreadLocalProvider;
import service_setup.testing.CreateAccount;
import service_setup.Account;
import service_setup.ServiceReplica;
import service_setup.testing.DepositMoney;
import service_setup.testing.Sleep;
import service_setup.testing.Task;
import service_setup.testing.WithdrawMoney;

class BasicFunctionalityTest {

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

    // run one isolated benchnark with 5 replicas and 100 persons
    @Test
    void runIsolatedBenchmark() {
        long benchmark = benchmark(2, 2);
        System.out.println("Benchmark took " + benchmark + " seconds");
    }

    @Test
    void runAccountOperationsTest() {
        // Define the number of accounts, operations, and replicas
        int numOfAccounts = 10;
        int numOfOperations = 100;
        int numOfReplicas = 2;

        // Create a list of service replicas
        List<ServiceReplica> serviceReplicas = IntStream.range(0, numOfReplicas).mapToObj(i -> {
            // Create a task queue
            BlockingQueue<Task> taskQueue = new LinkedBlockingQueue<>();

            // Create accounts and add deposit and withdraw tasks to the queue
            for (int j = 0; j < numOfAccounts; j++) {
                Account account = getAccount(i, j, 0);
                taskQueue.add(new CreateAccount(account.getName(), account.getBalance(), getSimulatedDelay()));
                taskQueue.add(new Sleep(1000));
                taskQueue.add(new Sleep(100));
                for (int k = 0; k < numOfOperations; k++) {
                    taskQueue.add(new DepositMoney(account.getName(), 100));
                    taskQueue.add(new WithdrawMoney(account.getName(), 50));
                }
            }

            // Create a service replica with the task queue
            return new ServiceReplica(String.valueOf(i), taskQueue);
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
                    assertEquals(5000, account.getBalance(), "Account balance is incorrect");
                });
            }
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
            taskQueue.add(new Sleep(100));
            taskQueue.add(new Sleep(100));
            taskQueue.add(new Sleep(100));
            taskQueue.add(new Sleep(100));
            taskQueue.add(new Sleep(100));
            taskQueue.add(new Sleep(100));
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
            return new ServiceReplica(String.valueOf(i), taskQueue);
        }).toList();

        // start the service replicas
        serviceReplicas.forEach(Thread::start);

        // wait for the service replicas to finish
        while (serviceReplicas.stream().anyMatch(ServiceReplica::isRunning)) {
            Thread.onSpinWait();
        }
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
        taskQueue.add(new Sleep(1000));
        for (int i = 0; i < numOfPersons; i++) {
            taskQueue.add(new CreateAccount(
                    getAccount(replicaNumber, i, 0).getName(), getAccount(replicaNumber, i, 0).getBalance(), getSimulatedDelay()
            ));
        }
        int i = numOfReplicas * numOfPersons;
        for (int i1 = 0; i1 < i; i1++) {
            taskQueue.add(new Sleep(10));
        }
    }

    private static Account getAccount(int replicaNumber, int accountNumber, int balance) {
        return new Account(replicaNumber + "john" + accountNumber, balance);
    }

    private static int getSimulatedDelay() {
        //return 0;
        return new Random().nextInt(0, 5);
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
}
