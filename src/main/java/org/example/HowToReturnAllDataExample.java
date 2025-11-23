package org.example;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Simple examples showing how to return ALL data from the trading API.
 * Run this as a standalone program to see the different approaches.
 */
public class HowToReturnAllDataExample {




    /**
     * EXAMPLE 2: Fetch ALL historical + real-time data
     * ⚠️ IMPORTANT: Must use limit() to avoid blocking forever!
     */
    static void example2_AllHistoricalPlusRealtime() {
        System.out.println("\n🔄 EXAMPLE 2: Fetch Historical + Real-time");
        System.out.println("-".repeat(70));

        MockTradesApi api = createApiWithSampleData(100);
        TradesService service = new TradesService(api);
        Instant since = Instant.now().minus(1, ChronoUnit.HOURS);

        // Start a thread to publish real-time trades
        Thread publisher = new Thread(() -> {
            try {
                Thread.sleep(50); // Let consumer connect first
                for (int i = 0; i < 25; i++) {
                    api.publishRealtimeTrade(new Trade(
                            "realtime-" + i,
                            Instant.now(),
                            BigDecimal.valueOf(200 + i),
                            BigDecimal.ONE,
                            Side.SELL
                    ));
                    Thread.sleep(10);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        publisher.start();

        // Fetch all historical + 25 real-time trades
        List<Trade> allTrades = service.trades(since)
                .limit(125) // 100 historical + 25 real-time
                .collect(Collectors.toList());

        try {
            publisher.join(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("✅ Fetched " + allTrades.size() + " trades total");
        long historicalCount = allTrades.stream()
                .filter(t -> t.tradeId().startsWith("trade-"))
                .count();
        long realtimeCount = allTrades.stream()
                .filter(t -> t.tradeId().startsWith("realtime-"))
                .count();
        System.out.println("   - Historical: " + historicalCount);
        System.out.println("   - Real-time: " + realtimeCount);
        System.out.println("⚠️  WARNING: Always use .limit() or .takeWhile() with trades(since)!");
    }

    /**
     * EXAMPLE 4: Manual pagination for large datasets
     * ✅ RECOMMENDED when memory is constrained
     */
    static void example4_ManualPagination() {
        System.out.println("\n📚 EXAMPLE 4: Manual Pagination (Large Datasets)");
        System.out.println("-".repeat(70));

        MockTradesApi api = createApiWithSampleData(750);
        Instant since = Instant.now().minus(1, ChronoUnit.HOURS);

        List<Trade> allTrades = new java.util.ArrayList<>();
        int offset = 0;
        int pageSize = 100;
        int pageCount = 0;

        while (true) {
            List<Trade> page = api.query(since, offset, pageSize);
            if (page.isEmpty()) break;

            pageCount++;
            System.out.println("   Page " + pageCount + ": " + page.size() + " trades");

            allTrades.addAll(page);

            if (page.size() < pageSize) break; // Last page
            offset += page.size();
        }

        System.out.println("✅ Fetched " + allTrades.size() + " trades in " + pageCount + " pages");
        System.out.println("💡 Use pagination for very large datasets or streaming processing");
    }

    /**
     * Helper: Create API with sample data
     */
    private static MockTradesApi createApiWithSampleData(int count) {
        MockTradesApi api = new MockTradesApi();
        Instant baseTime = Instant.now().minus(1, ChronoUnit.HOURS);

        for (int i = 0; i < count; i++) {
            api.addTrade(new Trade(
                    "trade-" + i,
                    baseTime.plus(i, ChronoUnit.SECONDS),
                    BigDecimal.valueOf(100 + i % 50),
                    BigDecimal.valueOf(1.0 + (i % 10) * 0.5),
                    i % 2 == 0 ? Side.BUY : Side.SELL
            ));
        }

        return api;
    }
}

