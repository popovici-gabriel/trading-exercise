# Programming task

Suppose you're writing an application for trading on power exchange (PX).
Your application needs to know which trades already happened or are happening
right now.

Your task will be to write a function `trades` which gets an instant
(`java.time.Instant`) and returns a stream of all trades
which happened since that instant in order they happened.

Your application uses following definitions

```kotlin
import java.math.BigDecimal
import java.time.Instant

enum class Side {
    Sell, Buy
}

data class Trade(
    val tradeId: String,
    val timestamp: Instant,
    val price: BigDecimal,
    val volume: BigDecimal,
    val side: Side
)
```

and PX promised to give you a thread-safe implementation of `TradesApi` interface:

```kotlin
interface TradesApi {
    fun realtimeUpdates(): Iterable<Trade>
    fun query(since: Instant): List<Trade>
}
```

Here is an excerpt from the documentation of `realtimeUpdates`:

> `realtimeUpdates` is used to obtain an iterator with
> trades which are currently happening.
>
> The call `.iterator()` always succeeds and returns an iterator
> which is **not** connected to the server. The first call to `.hasNext()`
> connects to the server and starts receiving events about trades.
> Every call to `.next()` must be preceded by a call to `.hasNext()`.
>
> Events on the server are buffered. When the buffer gets full,
> your application will be disconnected, and the iterator will throw an exception.
>
> The iterator returns trades in order they happened.


Here is an excerpt from the documentation of `realtimeUpdates`:

> `query(since)` is used to obtain a list of already existing trades.
> `since` is an inclusive bound.
> The list contains at most 100 trades.
> The list is sorted by `timestamp`.

## Notes

1. Function `trades` which you're supposed to write
   should have at least one parameter (an instant), but it may
   have more parameters.
2. If a trade is returned by `realtimeUpdates` it may not be immediately
   returned by `query`. `query` will return it eventually.
3. You can assume that every minute at least one trade is executed.
4. The function `trades` can return a stream or an iterator
   or a flow or a user can give it a callback.
   You can use any streaming library e.g. RxJava, Akka Streams,
   Kotlin coroutines, Reactor, etc.
5. It would be nice if you write a few tests.

# Bonus questions

If you have finished the programming task and still have some time:

1. What do you think about `TradesApi`?
   Could it be improved?
2. What happens if there are more than 100 trades with the same timestamp?
 