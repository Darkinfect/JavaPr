package step2.classes;

import java.io.Serial;

public class Designer extends Worker {
    @Serial
    private static final long serialVersionUID = 1L;

    public Designer(String name, int age, double salary) {
        super(name, age, salary);
    }

    @Override
    public void work(UTF8BundleReader bundle) {
        System.out.println(bundle.getString("designer_work") + " " + name);
    }
}
