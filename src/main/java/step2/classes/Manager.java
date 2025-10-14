package step2.classes;

import lombok.Getter;

import java.io.Serial;

public class Manager extends Worker {
    @Serial
    private static final long serialVersionUID = 1L;
    @Getter
    private static int managerCount = 0;

    public Manager(String name, int age, double salary) {
        super(name, age, salary);
        managerCount++;
    }

    @Override
    public void work(UTF8BundleReader resourceBundle) {
        System.out.println(resourceBundle.getString("manager_work") + " " + name);
    }
}
