package step2;

import step2.classes.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        Locale locale = Locale.of(args[0],args[1]);
        UTF8BundleReader bundle = new UTF8BundleReader("messages", locale);

        WorkerConnection connector = new WorkerConnection("workers.dat");

        try {
            List<Worker> workers = new ArrayList<>();
            workers.add(new Manager("Иван Иванов", 35, 80000));
            workers.add(new Analyst("Косцов Артём", 28, 60000));
            workers.add(new Programmer("Каленик Илья", 30, 90000));
            workers.add(new Tester("Илюша Тестер", 25, 50000));
            workers.add(new Designer("Бутрамеев Иван Павлович", 27, 70000));

            connector.saveWorkers(workers);

            List<Worker> loadedWorkers = connector.loadWorkers();
            for (Worker w : loadedWorkers) {
                System.out.println(w);
                w.work(bundle);
            }

        } catch (IllegalArgumentException | IOException | ClassNotFoundException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }
}
