package org.example;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Mock implementation of TradesApi for testing purposes.
 */
public class MockTradesApi implements TradesApi {

    public static final int MAX_SIZE = 100;

    private final List<Trade> allTrades = Collections.synchronizedList(new ArrayList<>());

    private final BlockingQueue<Trade> realtimeQueue = new PriorityBlockingQueue<>(MAX_SIZE,
            Comparator.comparing(Trade::timestamp).thenComparing(Trade::tradeId));

    private volatile boolean realtimeActive = false;

    public void addTrade(Trade trade) {
        allTrades.add(trade);
        if (realtimeActive) {
            realtimeQueue.offer(trade);
        }
    }

    public void addTrades(Trade... trades) {
        for (Trade trade : trades) {
            addTrade(trade);
        }
    }

    public void publishRealtimeTrade(Trade trade) {
        realtimeQueue.offer(trade);
    }

    @Override
    public Iterable<Trade> realtimeUpdates() {
        return () -> new Iterator<>() {
            private boolean connected = false;

            private Trade nextTrade = null;

            @Override
            public boolean hasNext() {
                if (!connected) {
                    connected = true;
                    realtimeActive = true;
                }

                if (nextTrade != null) {
                    return true;
                }

                try {
                    nextTrade = realtimeQueue.poll(MAX_SIZE, TimeUnit.MILLISECONDS);
                    return nextTrade != null;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }

            @Override
            public Trade next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                Trade result = nextTrade;
                nextTrade = null;
                return result;
            }
        };
    }

    @Override
    public List<Trade> query(Instant since) {
        return query(since, 0, MAX_SIZE);
    }

    @Override
    public List<Trade> query(Instant since, int offset, int limit) {
        synchronized (allTrades) {
            return allTrades
                    .stream()
                    .filter(trade -> !trade.timestamp().isBefore(since))
                    .sorted(Comparator.comparing(Trade::timestamp))
                    .skip(offset)
                    .limit(limit)
                    .toList();
        }
    }

    @Override
    public List<Trade> query(Instant since, String tradeId, int limit) {
        synchronized (allTrades) {
            return allTrades
                    .stream()
                    .filter(trade -> !trade.timestamp().isBefore(since))
                    .filter(trade -> trade.tradeId().compareTo(tradeId) > 0)
                    .sorted(Comparator.comparing(Trade::tradeId))
                    .limit(limit)
                    .toList();
        }
    }
}

