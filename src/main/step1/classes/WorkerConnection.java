package step1.classes;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class WorkerConnection {
    private String filename;

    public WorkerConnection(String filename) {
        this.filename = filename;
    }

    public void saveWorkers(List<Worker> workers) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(workers);
        } catch (OutOfMemoryError e) {
            throw new IOException("Недостаточно памяти для сохранения файла", e);
        }
    }

    public List<Worker> loadWorkers() throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            Object obj = ois.readObject();
            if (obj instanceof List) {
                return (List<Worker>) obj;
            } else {
                throw new IOException("В файле отсутствуют ожидаемые данные");
            }
        }
    }

    public void addWorker(Worker worker) throws IOException, ClassNotFoundException {
        List<Worker> workers = new ArrayList<>();
        try {
            workers = loadWorkers();
        } catch (FileNotFoundException e) {
            System.out.println(e.getStackTrace());
        }
        workers.add(worker);
        saveWorkers(workers);
    }
}
