package org.example;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public record Trade(
    String tradeId,
    Instant timestamp,
    BigDecimal price,
    BigDecimal volume,
    Side side
) {
    public Trade {
        Objects.requireNonNull(tradeId, "tradeId cannot be null");
        Objects.requireNonNull(timestamp, "timestamp cannot be null");
        Objects.requireNonNull(price, "price cannot be null");
        Objects.requireNonNull(volume, "volume cannot be null");
        Objects.requireNonNull(side, "side cannot be null");
    }
}

