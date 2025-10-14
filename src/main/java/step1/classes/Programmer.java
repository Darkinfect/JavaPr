package step1.classes;

import java.io.Serial;

public class Programmer extends Worker {
    @Serial
    private static final long serialVersionUID = 1L;

    public Programmer(String name, int age, double salary) {
        super(name, age, salary);
    }

    @Override
    public void work() {
        System.out.println("Программист " + name + " пишет код.");
    }
}
