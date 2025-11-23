package org.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TradesServiceTest {
    private MockTradesApi mockApi;

    private TradesService service;

    private Instant baseTime;

    @BeforeEach
    void setUp() {
        mockApi = new MockTradesApi();
        service = new TradesService(mockApi);
        baseTime = Instant.parse("2025-11-21T10:00:00Z");
    }

    @Test
    @DisplayName("Should return empty stream when no trades exist")
    void testNoTrades() {
        Stream<Trade> trades = service.trades(baseTime);
        assertEquals(0, trades.count());
    }

    @Test
    @DisplayName("Should return historical trades in chronological order")
    void testHistoricalTradesOnly() {
        // Add trades out of order
        Trade trade1 = createTrade("1", baseTime.plusSeconds(10), "100.50", "10");
        Trade trade2 = createTrade("2", baseTime.plusSeconds(5), "100.25", "15");
        Trade trade3 = createTrade("3", baseTime.plusSeconds(20), "100.75", "20");

        mockApi.addTrades(trade1, trade2, trade3);

        List<Trade> result = service.trades(baseTime).toList();

        assertEquals(3, result.size());
        // Verify chronological order
        assertEquals("2", result.get(0).tradeId());
        assertEquals("1", result.get(1).tradeId());
        assertEquals("3", result.get(2).tradeId());
    }

    private Trade createTrade(String id, Instant timestamp, String price, String volume) {
        return new Trade(id, timestamp, new BigDecimal(price), new BigDecimal(volume), Side.BUY);
    }
}

