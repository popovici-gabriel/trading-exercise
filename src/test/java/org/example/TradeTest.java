package org.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TradeTest {

    @Test
    @DisplayName("Should create a valid trade with all fields")
    void testCreateTrade() {
        Instant now = Instant.now();
        Trade trade = new Trade(
            "TRADE-001",
            now,
            new BigDecimal("100.50"),
            new BigDecimal("10.5"),
            Side.BUY
        );

        assertEquals("TRADE-001", trade.tradeId());
        assertEquals(now, trade.timestamp());
        assertEquals(new BigDecimal("100.50"), trade.price());
        assertEquals(new BigDecimal("10.5"), trade.volume());
        assertEquals(Side.BUY, trade.side());
    }

    @Test
    @DisplayName("Should throw exception when tradeId is null")
    void testNullTradeId() {
        assertThrows(NullPointerException.class, () -> {
            new Trade(
                null,
                Instant.now(),
                new BigDecimal("100.50"),
                new BigDecimal("10.5"),
                Side.BUY
            );
        });
    }
}

