package de.anamurray.lager.service;

import de.anamurray.lager.model.Article;
import de.anamurray.lager.model.InsufficientStockException;
import de.anamurray.lager.model.MovementType;
import de.anamurray.lager.repository.InMemoryArticleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class InventoryServiceTest {

    private static final Clock FIXED = Clock.fixed(Instant.parse("2027-05-03T08:00:00Z"), ZoneId.of("UTC"));

    private InventoryService service;

    @BeforeEach
    void setUp() {
        service = new InventoryService(new InMemoryArticleRepository(), FIXED);
        service.createArticle(new Article("A-1001", "Europalette", new BigDecimal("12.90"), 100, 50));
        service.createArticle(new Article("A-1002", "Stretchfolie", new BigDecimal("8.45"), 35, 40));
        service.createArticle(new Article("A-1003", "Packband", new BigDecimal("2.30"), 60, 100));
    }

    @Test
    void duplicateArticleNumberIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                service.createArticle(new Article("A-1001", "Doppelt", BigDecimal.ONE, 1, 1)));
    }

    @Test
    void incomingAndOutgoingAreBookedAndJournaled() {
        service.bookIncoming("A-1001", 20);
        service.bookOutgoing("A-1001", 70);

        assertEquals(50, service.getArticle("A-1001").getStock());
        assertEquals(2, service.journal().size());
        assertEquals(MovementType.WARENAUSGANG, service.journal().get(1).type());
        assertEquals(LocalDateTime.of(2027, 5, 3, 8, 0), service.journal().get(0).timestamp());
    }

    @Test
    void failedOutgoingIsNotJournaled() {
        assertThrows(InsufficientStockException.class, () -> service.bookOutgoing("A-1002", 36));
        assertTrue(service.journal().isEmpty());
        assertEquals(35, service.getArticle("A-1002").getStock());
    }

    @Test
    void unknownArticleThrows() {
        assertThrows(NoSuchElementException.class, () -> service.bookIncoming("A-9999", 1));
    }

    @Test
    void reorderListIsSortedByShortfall() {
        // A-1003 fehlen 40 Stück, A-1002 fehlen 5 Stück, A-1001 ist ausreichend
        var list = service.reorderList();
        assertEquals(2, list.size());
        assertEquals("A-1003", list.get(0).getArticleNumber());
        assertEquals("A-1002", list.get(1).getArticleNumber());
    }

    @Test
    void totalStockValue() {
        // 100 × 12,90 + 35 × 8,45 + 60 × 2,30 = 1290,00 + 295,75 + 138,00
        assertEquals(new BigDecimal("1723.75"), service.totalStockValue());
    }
}
