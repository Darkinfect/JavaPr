package step2.classes;

import lombok.Getter;

import javax.swing.text.DateFormatter;
import java.io.Serial;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.SimpleFormatter;

public abstract class Worker implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @Getter
    protected String name;
    @Getter
    protected int age;
    @Getter
    protected transient double salary;
    @Getter
    protected Date creationDate;

    public Worker(String name, int age, double salary) {
        if (age < 18 || age > 70) {
            throw new IllegalArgumentException("Возраст должен быть от 18 до 70");
        }
        if (salary < 0) {
            throw new IllegalArgumentException("Зарплата должна быть неотрицательной");
        }
        this.name = name;
        this.age = age;
        this.salary = salary;
        this.creationDate = new Date();
    }

    public abstract void work(UTF8BundleReader reader);
    public void setSalary(double salary) {
        if (salary < 0) {
            throw new IllegalArgumentException("Зарплата должна быть неотрицательной");
        }
        this.salary = salary;
    }

    @Override
    public String toString() {
        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss yyyy-MM-dd");
        String format = formatter.format(creationDate);
        return String.format("%s [Имя=%s, Возраст=%d, Зарплата=%.2f, Время создания= %s ]",
                this.getClass().getSimpleName(), name, age, salary,format);
    }
}
