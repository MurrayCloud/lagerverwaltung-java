package de.anamurray.lager.ui;

import de.anamurray.lager.model.Article;
import de.anamurray.lager.model.StockMovement;
import de.anamurray.lager.service.InventoryService;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/** Einfache Konsolenoberfläche. Enthält keine Geschäftslogik – nur Ein- und Ausgabe. */
public class ConsoleMenu {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final InventoryService service;
    private final Scanner in;

    public ConsoleMenu(InventoryService service, Scanner in) {
        this.service = service;
        this.in = in;
    }

    public void run() {
        while (true) {
            System.out.println("""

                    ===== LAGERVERWALTUNG =====
                    1  Alle Artikel anzeigen
                    2  Artikel anlegen
                    3  Wareneingang buchen
                    4  Warenausgang buchen
                    5  Nachbestellliste
                    6  Gesamter Lagerwert
                    7  Buchungsjournal
                    0  Beenden""");
            String choice = prompt("Auswahl");
            if (choice.equals("0")) {
                return;
            }
            try {
                switch (choice) {
                    case "1" -> printArticles(service.allArticles());
                    case "2" -> createArticle();
                    case "3" -> service.bookIncoming(prompt("Artikelnummer"), promptInt("Menge"));
                    case "4" -> service.bookOutgoing(prompt("Artikelnummer"), promptInt("Menge"));
                    case "5" -> printArticles(service.reorderList());
                    case "6" -> System.out.println("Lagerwert: " + service.totalStockValue() + " €");
                    case "7" -> printJournal(service.journal());
                    default -> System.out.println("Ungültige Auswahl.");
                }
            } catch (RuntimeException e) {
                // Fachliche Fehler werden verständlich angezeigt, das Programm läuft weiter.
                System.out.println("Fehler: " + e.getMessage());
            }
        }
    }

    private void createArticle() {
        Article article = new Article(
                prompt("Artikelnummer (A-0000)"),
                prompt("Bezeichnung"),
                new BigDecimal(prompt("Stückpreis (z. B. 12,50)").replace(',', '.')),
                promptInt("Anfangsbestand"),
                promptInt("Mindestbestand"));
        service.createArticle(article);
        System.out.println("Angelegt: " + article);
    }

    private void printArticles(List<Article> articles) {
        if (articles.isEmpty()) {
            System.out.println("(keine Einträge)");
        }
        articles.forEach(a -> System.out.println((a.needsReorder() ? "! " : "  ") + a));
    }

    private void printJournal(List<StockMovement> movements) {
        if (movements.isEmpty()) {
            System.out.println("(noch keine Buchungen in dieser Sitzung)");
        }
        movements.forEach(m -> System.out.printf("%s  %-13s %s  %5d%n",
                m.timestamp().format(TIME), m.type(), m.articleNumber(), m.quantity()));
    }

    private String prompt(String label) {
        System.out.print(label + ": ");
        return in.nextLine().trim();
    }

    private int promptInt(String label) {
        try {
            return Integer.parseInt(prompt(label));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Bitte eine ganze Zahl eingeben");
        }
    }
}
