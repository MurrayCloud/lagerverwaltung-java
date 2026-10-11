package de.anamurray.lager.repository;

import de.anamurray.lager.model.Article;

import java.util.List;
import java.util.Optional;

/**
 * Schnittstelle für die Datenhaltung.
 * Der Service kennt nur dieses Interface – ob die Daten in einer CSV-Datei,
 * im Speicher oder später in einer Datenbank liegen, ist austauschbar.
 */
public interface ArticleRepository {

    List<Article> findAll();

    Optional<Article> findByNumber(String articleNumber);

    void save(Article article);

    void delete(String articleNumber);
}
