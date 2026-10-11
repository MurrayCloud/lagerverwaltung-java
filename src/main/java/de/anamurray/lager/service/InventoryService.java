package de.anamurray.lager.service;

import de.anamurray.lager.model.Article;
import de.anamurray.lager.model.MovementType;
import de.anamurray.lager.model.StockMovement;
import de.anamurray.lager.repository.ArticleRepository;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

/** Geschäftslogik: Artikel anlegen, Wareneingang/-ausgang buchen, Auswertungen. */
public class InventoryService {

    private final ArticleRepository repository;
    private final Clock clock;
    private final List<StockMovement> journal = new ArrayList<>();

    public InventoryService(ArticleRepository repository) {
        this(repository, Clock.systemDefaultZone());
    }

    /** Die Uhr wird übergeben, damit Tests mit einem festen Zeitpunkt arbeiten können. */
    public InventoryService(ArticleRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public void createArticle(Article article) {
        if (repository.findByNumber(article.getArticleNumber()).isPresent()) {
            throw new IllegalArgumentException("Artikelnummer existiert bereits: " + article.getArticleNumber());
        }
        repository.save(article);
    }

    public void bookIncoming(String articleNumber, int quantity) {
        Article article = getArticle(articleNumber);
        article.increaseStock(quantity);
        repository.save(article);
        journal.add(new StockMovement(articleNumber, MovementType.WARENEINGANG, quantity, LocalDateTime.now(clock)));
    }

    public void bookOutgoing(String articleNumber, int quantity) {
        Article article = getArticle(articleNumber);
        article.decreaseStock(quantity); // wirft InsufficientStockException, der Bestand bleibt dann unverändert
        repository.save(article);
        journal.add(new StockMovement(articleNumber, MovementType.WARENAUSGANG, quantity, LocalDateTime.now(clock)));
    }

    public Article getArticle(String articleNumber) {
        return repository.findByNumber(articleNumber)
                .orElseThrow(() -> new NoSuchElementException("Artikel nicht gefunden: " + articleNumber));
    }

    public List<Article> allArticles() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(Article::getArticleNumber))
                .toList();
    }

    /** Alle Artikel, die nachbestellt werden müssen – der kritischste zuerst. */
    public List<Article> reorderList() {
        return repository.findAll().stream()
                .filter(Article::needsReorder)
                .sorted(Comparator.comparingInt(a -> a.getStock() - a.getMinimumStock()))
                .toList();
    }

    public BigDecimal totalStockValue() {
        return repository.findAll().stream()
                .map(Article::stockValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<StockMovement> journal() {
        return List.copyOf(journal);
    }
}
