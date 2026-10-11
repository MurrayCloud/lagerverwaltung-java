package de.anamurray.lager.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ArticleTest {

    private Article pallet() {
        return new Article("A-1001", "Europalette", new BigDecimal("12.90"), 10, 5);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "1001", "A1001", "A-10", "B-1001"})
    void rejectsInvalidArticleNumber(String number) {
        assertThrows(IllegalArgumentException.class,
                () -> new Article(number, "Test", BigDecimal.ONE, 0, 0));
    }

    @Test
    void rejectsNegativePrice() {
        assertThrows(IllegalArgumentException.class,
                () -> new Article("A-1001", "Test", new BigDecimal("-0.01"), 0, 0));
    }

    @Test
    void decreaseStockBeyondAvailableThrowsAndKeepsStock() {
        Article article = pallet();
        assertThrows(InsufficientStockException.class, () -> article.decreaseStock(11));
        assertEquals(10, article.getStock());
    }

    @Test
    void zeroQuantityIsNotAllowed() {
        assertThrows(IllegalArgumentException.class, () -> pallet().increaseStock(0));
    }

    @Test
    void needsReorderAtMinimumStock() {
        Article article = pallet();
        article.decreaseStock(4);
        assertFalse(article.needsReorder());
        article.decreaseStock(1);
        assertTrue(article.needsReorder());
    }

    @Test
    void stockValueUsesExactDecimalArithmetic() {
        Article article = new Article("A-2000", "Schraube", new BigDecimal("0.10"), 3, 0);
        // mit double wäre 0.1 * 3 = 0.30000000000000004
        assertEquals(new BigDecimal("0.30"), article.stockValue());
    }
}
