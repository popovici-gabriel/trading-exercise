package org.example;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Thread-safe API for accessing trades from the power exchange.
 */
public interface TradesApi {

    int SIZE = 100;

    Iterable<Trade> realtimeUpdates();

    /**
     * query by offset pagination
     */
    default List<Trade> query(Instant since) {
        return queryByCursor(since);
    }

    default Stream<Trade> stream(Instant since) {
        return queryByCursor(since).stream();
    }

    /**
     * query by offset pagination
     */
    default List<Trade> queryByOffset(Instant since) {
        var offset = 0;
        final var trades = new ArrayList<Trade>();
        while (true) {
            final var page = query(since, offset, SIZE);
            trades.addAll(page);
            if (page.isEmpty() || page.size() < SIZE) {
                break;
            }

            offset += page.size();
        }

        return trades;
    }

    /**
     * query by cursor pagination
     */
    default List<Trade> queryByCursor(Instant since) {
        var tradeId = UUID.randomUUID().toString();
        final var trades = new ArrayList<Trade>();
        while (true) {
            final var page = query(since, tradeId, SIZE);
            trades.addAll(page);
            if (page.isEmpty() || page.size() < SIZE) {
                break;
            }
            tradeId = page.getLast().tradeId();
        }

        return trades;
    }

    /**
     * Query trades with pagination support using offset and limit. - Offset based pagination
     *
     * @param since  start time (inclusive)
     * @param offset number of trades to skip
     * @param limit  maximum number of trades to return (use Integer.MAX_VALUE for all)
     * @return list of trades
     */
    List<Trade> query(Instant since, int offset, int limit);

    /**
     * Query trades with pagination support using cursor-based pagination. - Cursor based pagination
     *
     * @param since   start time (inclusive)
     * @param tradeId unique identifier of the last trade received
     * @param limit   maximum number of trades to return (use Integer.MAX_VALUE for all)
     * @return list of trades
     */
    List<Trade> query(Instant since, String tradeId, int limit);

}

