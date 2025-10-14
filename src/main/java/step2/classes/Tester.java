package step2.classes;

import java.io.Serial;

public class Tester extends Worker {
    @Serial
    private static final long serialVersionUID = 1L;

    public Tester(String name, int age, double salary) {
        super(name, age, salary);
    }

    @Override
    public void work(UTF8BundleReader bundle) {
        System.out.println(bundle.getString("tester_work") + " " + name);
    }
}
