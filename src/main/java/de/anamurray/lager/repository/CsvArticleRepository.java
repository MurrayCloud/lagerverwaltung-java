package de.anamurray.lager.repository;

import de.anamurray.lager.model.Article;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Speichert die Artikel in einer CSV-Datei (Trennzeichen ";", wie in deutschem Excel).
 * Format: artikelnummer;bezeichnung;preis;bestand;mindestbestand
 */
public class CsvArticleRepository extends InMemoryArticleRepository {

    private static final String HEADER = "artikelnummer;bezeichnung;preis;bestand;mindestbestand";
    private final Path file;

    public CsvArticleRepository(Path file) {
        this.file = file;
        load();
    }

    private void load() {
        if (!Files.exists(file)) {
            return;
        }
        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            for (String line : lines.subList(1, lines.size())) {
                if (line.isBlank()) {
                    continue;
                }
                String[] f = line.split(";", -1);
                super.save(new Article(f[0], f[1], new BigDecimal(f[2]),
                        Integer.parseInt(f[3]), Integer.parseInt(f[4])));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("CSV-Datei konnte nicht gelesen werden: " + file, e);
        }
    }

    private void persist() {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        for (Article a : articles.values()) {
            lines.add(String.join(";", a.getArticleNumber(), a.getName().replace(";", ","),
                    a.getUnitPrice().toPlainString(), String.valueOf(a.getStock()),
                    String.valueOf(a.getMinimumStock())));
        }
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("CSV-Datei konnte nicht geschrieben werden: " + file, e);
        }
    }

    @Override
    public void save(Article article) {
        super.save(article);
        persist();
    }

    @Override
    public void delete(String articleNumber) {
        super.delete(articleNumber);
        persist();
    }
}
