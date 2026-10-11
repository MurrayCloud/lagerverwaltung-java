package de.anamurray.lager.repository;

import de.anamurray.lager.model.Article;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Hält die Artikel nur im Arbeitsspeicher – praktisch für Tests. */
public class InMemoryArticleRepository implements ArticleRepository {

    protected final Map<String, Article> articles = new LinkedHashMap<>();

    @Override
    public List<Article> findAll() {
        return new ArrayList<>(articles.values());
    }

    @Override
    public Optional<Article> findByNumber(String articleNumber) {
        return Optional.ofNullable(articles.get(articleNumber));
    }

    @Override
    public void save(Article article) {
        articles.put(article.getArticleNumber(), article);
    }

    @Override
    public void delete(String articleNumber) {
        articles.remove(articleNumber);
    }
}
