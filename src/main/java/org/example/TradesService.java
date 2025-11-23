package org.example;

import static java.util.Spliterators.spliteratorUnknownSize;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class TradesService {
    private final TradesApi tradesApi;

    public TradesService(TradesApi tradesApi) {
        this.tradesApi = Objects.requireNonNull(tradesApi, "tradesApi cannot be null");
    }

    /**
     * Aggregates historical and real-time trades with a limit.
     * <p>
     *
     * @param since start time (inclusive)
     * @return stream of trades from both sources
     */
    public Stream<Trade> trades(Instant since) {
        Objects.requireNonNull(since, "since cannot be null");

        // Fetch all historical trades with pagination
        List<Trade> historicalTrades = tradesApi.query(since);

        // Real-time stream: filter out duplicates and trades before cutoff
        Stream<Trade> realtimeStream = StreamSupport
                .stream(spliteratorUnknownSize(tradesApi.realtimeUpdates().iterator(), Spliterator.ORDERED | Spliterator.NONNULL), false)
                .filter(trade -> !trade.timestamp().isBefore(since));

        // Concatenate and apply limit
        return Stream.concat(historicalTrades.stream(), realtimeStream);
    }

}

