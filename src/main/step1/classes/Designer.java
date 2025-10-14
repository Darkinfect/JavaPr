package step1.classes;

import java.io.Serial;

public class Designer extends Worker {
    @Serial
    private static final long serialVersionUID = 1L;

    public Designer(String name, int age, double salary) {
        super(name, age, salary);
    }

    @Override
    public void work() {
        System.out.println("Дизайнер " + name + " создаёт дизайн.");
    }
}
