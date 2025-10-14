package step1.classes;

import java.io.Serial;

public class Manager extends Worker{
    @Serial
    private static final long serialVersionUID = 1L;

    private static int managerCount = 0; // static поле

    public Manager(String name, int age, double salary) {
        super(name, age, salary);
        managerCount++;
    }

    @Override
    public void work() {
        System.out.println("Менеджер " + name + " организует работу команды.");
    }

    public static int getManagerCount() {
        return managerCount;
    }
}
