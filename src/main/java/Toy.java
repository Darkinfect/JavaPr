import lombok.Getter;

import java.io.*;
import java.time.LocalDate;
import java.util.Objects;
public class Toy implements Serializable{
    @Serial
    private static final long serialVersionUID = 1L;

    @Getter
    private final String code;
    @Getter
    private final String name;
    @Getter
    private final int minAge;
    @Getter
    private final int maxAge;
    @Getter
    private final double price;
    @Getter
    private final int quantity;
    @Getter
    private final LocalDate arrivalDate;
    @Getter
    private final String supplier;

    public Toy(String code, String name, int minAge, int maxAge,
               double price, int quantity, LocalDate arrivalDate, String supplier) {
        validateInput(code, name, minAge, maxAge, price, quantity, supplier);
        this.code = code;
        this.name = name;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.price = price;
        this.quantity = quantity;
        this.arrivalDate = arrivalDate;
        this.supplier = supplier;
    }

    private void validateInput(String code, String name, int minAge, int maxAge,
                               double price, int quantity, String supplier) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Код игрушки не может быть пустым");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Название не может быть пустым");
        }
        if (minAge < 0 || maxAge < 0 || minAge > maxAge) {
            throw new IllegalArgumentException("Некорректные возрастные границы");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Цена не может быть отрицательной");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Количество не может быть отрицательным");
        }
        if (supplier == null || supplier.trim().isEmpty()) {
            throw new IllegalArgumentException("Поставщик не может быть пустым");
        }
    }
    public String getAgeRange() {
        return minAge + "-" + maxAge;
    }

    @Override
    public String toString() {
        return String.format("Код: %s | Название: %s | Возраст: %d-%d | " +
                        "Цена: %.2f | Количество: %d | Дата: %s | Поставщик: %s",
                code, name, minAge, maxAge, price, quantity, arrivalDate, supplier);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Toy toy = (Toy) o;
        return Objects.equals(code, toy.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}
