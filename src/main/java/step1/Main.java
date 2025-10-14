package step1;

import step1.classes.*;

import java.io.IOException;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        WorkerConnection connection = new WorkerConnection("some.txt");
        try{
            List<Worker> workers = getWorkerList();

            connection.saveWorkers(workers);

            List<Worker> loadedWorkers = connection.loadWorkers();
            for (Worker w : loadedWorkers) {
                System.out.println(w);
                w.work();
            }
        }catch (IllegalArgumentException | IOException | ClassNotFoundException e){
            System.err.println("Ошибка: " + e.getMessage());
        }
    }

    private static List<Worker> getWorkerList() {
        Manager m = new Manager("Иван Иванов", 35, 80000);
        Analyst a = new Analyst("Анна Петрова", 28, 60000);
        Programmer p = new Programmer("Сергей Смирнов", 30, 90000);
        Tester t = new Tester("Елена Кузнецова", 25, 50000);
        Designer d = new Designer("Ольга Васильева", 27, 70000);

        List<Worker> workers = new ArrayList<>();
        workers.add(m);
        workers.add(a);
        workers.add(p);
        workers.add(t);
        workers.add(d);
        return workers;
    }
}
