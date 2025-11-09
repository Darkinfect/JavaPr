package main.java;

import javafx.beans.property.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class Toy implements Serializable {
    private static final long serialVersionUID = 1L;

    private String code;
    private String name;
    private int minAge;
    private int maxAge;
    private double price;
    private int quantity;
    private LocalDate arrivalDate;
    private String supplier;
    public Toy(String code, String name, int minAge, int maxAge,
               double price, int quantity, LocalDate arrivalDate, String supplier) {
        this.code = code;
        this.name = name;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.price = price;
        this.quantity = quantity;
        this.arrivalDate = arrivalDate;
        this.supplier = supplier;
    }
    public String getCode() { return code; }
    public String getName() { return name; }
    public int getMinAge() { return minAge; }
    public int getMaxAge() { return maxAge; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public LocalDate getArrivalDate() { return arrivalDate; }
    public String getSupplier() { return supplier; }
    public void setCode(String code) { this.code = code; }
    public void setName(String name) { this.name = name; }
    public void setMinAge(int minAge) { this.minAge = minAge; }
    public void setMaxAge(int maxAge) { this.maxAge = maxAge; }
    public void setPrice(double price) { this.price = price; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setArrivalDate(LocalDate arrivalDate) { this.arrivalDate = arrivalDate; }
    public void setSupplier(String supplier) { this.supplier = supplier; }
    public String getAgeRange() {
        return minAge + "-" + maxAge;
    }

    @Override
    public String toString() {
        return "Toy{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", minAge=" + minAge +
                ", maxAge=" + maxAge +
                ", price=" + price +
                ", quantity=" + quantity +
                ", arrivalDate=" + arrivalDate +
                ", supplier='" + supplier + '\'' +
                '}';
    }
}