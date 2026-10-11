package de.anamurray.lager.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Ein Artikel im Lager.
 * Der Bestand wird nur über {@link #increaseStock(int)} und {@link #decreaseStock(int)}
 * verändert, damit er nie negativ werden kann (Kapselung).
 */
public class Article {

    private final String articleNumber;
    private final String name;
    private final BigDecimal unitPrice;
    private final int minimumStock;
    private int stock;

    public Article(String articleNumber, String name, BigDecimal unitPrice, int stock, int minimumStock) {
        if (articleNumber == null || !articleNumber.matches("A-\\d{4}")) {
            throw new IllegalArgumentException("Artikelnummer muss das Format A-0000 haben: " + articleNumber);
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Bezeichnung darf nicht leer sein");
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new IllegalArgumentException("Preis darf nicht negativ sein");
        }
        if (stock < 0 || minimumStock < 0) {
            throw new IllegalArgumentException("Bestand und Mindestbestand dürfen nicht negativ sein");
        }
        this.articleNumber = articleNumber;
        this.name = name.trim();
        this.unitPrice = unitPrice.setScale(2, RoundingMode.HALF_UP);
        this.stock = stock;
        this.minimumStock = minimumStock;
    }

    public void increaseStock(int quantity) {
        requirePositive(quantity);
        stock += quantity;
    }

    public void decreaseStock(int quantity) {
        requirePositive(quantity);
        if (quantity > stock) {
            throw new InsufficientStockException(articleNumber, stock, quantity);
        }
        stock -= quantity;
    }

    /** true, wenn der Bestand den Mindestbestand erreicht oder unterschritten hat. */
    public boolean needsReorder() {
        return stock <= minimumStock;
    }

    /** Lagerwert = Bestand × Stückpreis. BigDecimal statt double, weil es um Geld geht. */
    public BigDecimal stockValue() {
        return unitPrice.multiply(BigDecimal.valueOf(stock));
    }

    private static void requirePositive(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Menge muss größer als 0 sein");
        }
    }

    public String getArticleNumber() { return articleNumber; }
    public String getName() { return name; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public int getStock() { return stock; }
    public int getMinimumStock() { return minimumStock; }

    @Override
    public boolean equals(Object o) {
        return o instanceof Article other && articleNumber.equals(other.articleNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(articleNumber);
    }

    @Override
    public String toString() {
        return "%s  %-28s %6d Stk.  %9s €".formatted(articleNumber, name, stock, unitPrice);
    }
}
