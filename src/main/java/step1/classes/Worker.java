package step1.classes;

import java.io.Serial;
import java.io.Serializable;

public abstract class Worker implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    protected String name;
    protected int age;
    protected transient double salary;
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
    }

    public abstract void work();

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }
    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        if (salary < 0) {
            throw new IllegalArgumentException("Зарплата должна быть неотрицательной");
        }
        this.salary = salary;
    }

    @Override
    public String toString() {
        return String.format("%s [Имя=%s, Возраст=%d, Зарплата=%.2f]",
                this.getClass().getSimpleName(), name, age, salary);
    }
}
