package de.anamurray.lager.model;

import java.time.LocalDateTime;

/** Eine Lagerbewegung. Unveränderlich, deshalb als record. */
public record StockMovement(String articleNumber, MovementType type, int quantity, LocalDateTime timestamp) {
}
