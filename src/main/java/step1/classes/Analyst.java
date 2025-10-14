package step1.classes;

import java.io.Serial;

public class Analyst extends Worker{
    @Serial
    private static final long serialVersionUID = 1L;

    public Analyst(String name, int age, double salary) {
        super(name, age, salary);
    }

    @Override
    public void work() {
        System.out.println("Аналитик " + name + " анализирует данные.");
    }
}
