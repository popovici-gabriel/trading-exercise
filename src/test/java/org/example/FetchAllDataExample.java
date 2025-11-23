package org.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

/**
 * Demonstrates different ways to fetch ALL data from the TradesService
 */
class FetchAllDataExample {

    @Test
    void demonstrateFetchingAllDataIncludingRealtime() throws InterruptedException {
        MockTradesApi api = new MockTradesApi();
        TradesService service = new TradesService(api);
        Instant baseTime = Instant.now().minus(1, ChronoUnit.HOURS);

        // Add 50 historical trades
        for (int i = 0; i < 50; i++) {
            api.addTrade(new Trade(
                    "historical-" + i,
                    baseTime.plus(i, ChronoUnit.SECONDS),
                    BigDecimal.valueOf(100 + i),
                    BigDecimal.valueOf(1.0),
                    Side.BUY
            ));
        }

        // Start consuming trades in a separate thread
        Thread consumerThread = new Thread(() -> {
            List<Trade> allTrades = service.trades(baseTime)
                    .limit(60) // 50 historical + 10 real-time
                    .collect(Collectors.toList());

            System.out.println("Total trades (historical + real-time): " + allTrades.size());
            assertEquals(60, allTrades.size());
        });

        consumerThread.start();

        // Give the consumer time to fetch historical data
        Thread.sleep(100);

        // Publish 10 real-time trades
        Instant realtimeStart = baseTime.plus(50, ChronoUnit.SECONDS);
        for (int i = 0; i < 10; i++) {
            api.publishRealtimeTrade(new Trade(
                    "realtime-" + i,
                    realtimeStart.plus(i, ChronoUnit.SECONDS),
                    BigDecimal.valueOf(200 + i),
                    BigDecimal.valueOf(2.0),
                    Side.SELL
            ));
            Thread.sleep(10);
        }

        consumerThread.join(5000);
        assertFalse(consumerThread.isAlive(), "Consumer should have finished");
    }

    /**
     * Helper method showing manual pagination to fetch all data
     */
    private List<Trade> fetchAllViaPagination(MockTradesApi api, Instant since) {
        List<Trade> allTrades = new java.util.ArrayList<>();
        int offset = 0;
        int pageSize = 100;

        while (true) {
            List<Trade> page = api.query(since, offset, pageSize);
            if (page.isEmpty()) {
                break;
            }
            allTrades.addAll(page);
            if (page.size() < pageSize) {
                break; // Last page
            }
            offset += page.size();
        }

        return allTrades;
    }

}

