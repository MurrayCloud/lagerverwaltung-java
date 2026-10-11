package de.anamurray.lager.repository;

import de.anamurray.lager.model.Article;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CsvArticleRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void savedArticlesSurviveARestart() {
        Path file = tempDir.resolve("artikel.csv");

        CsvArticleRepository first = new CsvArticleRepository(file);
        first.save(new Article("A-1001", "Europalette", new BigDecimal("12.90"), 140, 50));
        first.save(new Article("A-1002", "Stretchfolie", new BigDecimal("8.45"), 35, 40));
        first.delete("A-1002");

        CsvArticleRepository second = new CsvArticleRepository(file);
        assertEquals(1, second.findAll().size());
        Article loaded = second.findByNumber("A-1001").orElseThrow();
        assertEquals("Europalette", loaded.getName());
        assertEquals(new BigDecimal("12.90"), loaded.getUnitPrice());
        assertEquals(140, loaded.getStock());
    }

    @Test
    void missingFileStartsEmpty() {
        assertTrue(new CsvArticleRepository(tempDir.resolve("gibt-es-nicht.csv")).findAll().isEmpty());
    }
}
