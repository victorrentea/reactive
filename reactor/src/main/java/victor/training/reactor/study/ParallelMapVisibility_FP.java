package victor.training.reactor.study;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * PROTOTYPE CODE FOR DISCUSSION PURPOSES ONLY - NOT PRODUCTION CODE
 * <p>
 * This prototype demonstrates the memory visibility question:
 * <p>
 * We use parallel() and flatMap() and perform a side effect (modifying a separate map
 * in each rail) without synchronization or marking fields volatile. We then have a
 * thenMany() operator that runs afterwards and relies on the state of the maps
 * reflecting all of that parallel activity - it gathers all of the keys and values
 * from each of the maps in each of the rails.
 * <p>
 * QUESTION: Is there any issue where the state of those maps may not have been
 * properly flushed (i.e., memory visibility issues)?
 */
public class ParallelMapVisibility_FP {
  private static final int PARALLELISM = 4;
  private static final int PREFETCH = 20;

  private final Scheduler parallelScheduler = Schedulers.newParallel("worker", PARALLELISM);

  /**
   * Demonstrates the pattern where:
   * 1. Items are processed in parallel rails
   * 2. Each rail modifies its own bucket of a shared MUTABLE data structure (BucketedState)
   * 3. After all parallel processing completes, thenMany() gathers results from all buckets,
   * merging values for the same key across different buckets
   */
  public Flux<Result> process(Flux<Item> items) {
    return items
        .parallel(PARALLELISM, PREFETCH)
        .runOn(parallelScheduler, PREFETCH)
        .groups()
        .flatMap(
            groupedFlux -> {
              // Each rail gets its own bucket ID
              int bucketId = groupedFlux.key();

              return groupedFlux
                  .map(item -> {
                    // NO SIDE EFFECT: just return key-value pairs to be merged at the end
                    return new KeyValue(item.key(), item.value());
                  });
            },
            PARALLELISM)
        .reduce(new HashMap<String, Set<String>>(),
            (accumulatedMap, keyValue) -> {
              // Accumulate key-value pairs into the map
              accumulatedMap
                  .computeIfAbsent(keyValue.key(), k -> new HashSet<>())
                  .add(keyValue.value());
              return accumulatedMap;
            })
        .flatMapIterable(HashMap::entrySet)
        .map(entry -> new Result(entry.getKey(), entry.getValue()));
  }

  record KeyValue(String key, String value) {
  }

  // Simple data classes
  record Item(String key, String value) {
  }

  record Result(String key, Set<String> values) {
  }

  /**
   * Main method to run the prototype and observe behavior
   */
  public static void main(String[] args) throws InterruptedException {
    ParallelMapVisibility_FP prototype = new ParallelMapVisibility_FP();

    // Create test data - items that will be distributed across parallel rails
    // Note: The same key (e.g., "sharedKeyA", "sharedKeyB") appears multiple times.
    // Since items are distributed to rails based on their index in the stream (not by key),
    // the same key can end up in different buckets with different values.
    // The gather phase will merge all values for each key across all buckets.
    Flux<Item> testItems = Flux.just(
        new Item("sharedKeyA", "value-A1"),   // index 0 -> bucket 0
        new Item("sharedKeyB", "value-B1"),   // index 1 -> bucket 1
        new Item("uniqueKey1", "value1"),     // index 2 -> bucket 2
        new Item("uniqueKey2", "value2"),     // index 3 -> bucket 3
        new Item("sharedKeyA", "value-A2"),   // index 4 -> bucket 0 (same key, same bucket)
        new Item("sharedKeyB", "value-B2"),   // index 5 -> bucket 1 (same key, same bucket)
        new Item("sharedKeyA", "value-A3"),   // index 6 -> bucket 2 (same key, different bucket)
        new Item("sharedKeyB", "value-B3"),   // index 7 -> bucket 3 (same key, different bucket)
        new Item("uniqueKey3", "value3"),     // index 8 -> bucket 0
        new Item("uniqueKey4", "value4"),     // index 9 -> bucket 1
        new Item("sharedKeyA", "value-A4"),   // index 10 -> bucket 2 (same key, same bucket as A3)
        new Item("sharedKeyB", "value-B4")    // index 11 -> bucket 3 (same key, same bucket as B3)
    );

    System.out.println("Starting parallel processing...");
    System.out.println("Items will be distributed across " + PARALLELISM + " parallel rails");
    System.out.println("NOTE: Same keys (sharedKeyA, sharedKeyB) can exist in multiple buckets");
    System.out.println("Each rail writes to its own bucket map (no synchronization, no volatile)");
    System.out.println("After parallel completes, thenMany gathers and MERGES values for each key across all buckets");
    System.out.println("---");

    prototype.process(testItems)
        .doOnNext(result -> System.out.printf(
            "[%s] Merged result for key '%s': %s%n",
            Thread.currentThread().getName(),
            result.key(),
            result.values()))
        .doOnComplete(() -> System.out.println("--- Processing complete ---"))
        .blockLast();

    // Dispose the scheduler to allow JVM to exit
    prototype.parallelScheduler.dispose();
  }
}
