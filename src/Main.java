import service_setup.ServiceReplica;

import java.util.stream.IntStream;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        System.out.println("System starting...");
        IntStream.range(0, 5).forEach(e -> {
            ServiceReplica serviceReplica = new ServiceReplica();
            serviceReplica.start();
        });
        System.out.println("System started.");
    }
}