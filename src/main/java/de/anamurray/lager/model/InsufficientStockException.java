package de.anamurray.lager.model;

/** Wird geworfen, wenn mehr ausgebucht werden soll, als auf Lager ist. */
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String articleNumber, int available, int requested) {
        super("Nicht genug Bestand für %s: verfügbar %d, angefordert %d"
                .formatted(articleNumber, available, requested));
    }
}
