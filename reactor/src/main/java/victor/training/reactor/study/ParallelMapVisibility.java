package victor.training.reactor.study;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
public class ParallelMapVisibility {
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
    return Flux.using(
        // Create shared state that will be modified by parallel rails
        () -> new BucketedState(PARALLELISM),

        state -> items
            .parallel(PARALLELISM, PREFETCH)
            .runOn(parallelScheduler, PREFETCH)
            .groups()
            .flatMap(
                groupedFlux -> {
                  // Each rail gets its own bucket ID
                  int bucketId = groupedFlux.key();

                  return groupedFlux
                      .doOnNext(item -> {
                        // SIDE EFFECT: Modifying bucket-specific map
                        // No synchronization, no volatile
                        // Each bucket's map is only touched by one rail
                        state.addToBucket(bucketId, item.key(), item.value());
                      });
                },
                PARALLELISM)
            // After all parallel rails complete, thenMany runs
            // QUESTION: Are the writes to the bucket maps visible here?

            // ANSWER YES: https://docs.google.com/document/d/10L034c6KTLYDW82O-uNIKy45suRjlB-kVAiCpBiXUOA/edit?tab=t.0

            // TODO why pre-aggregate per bucket before merging? go FP
            .thenMany(gatherResultsFromAllBuckets(state)),

        BucketedState::close
    );
  }

  /**
   * This runs after all parallel processing completes.
   * It needs to see all the writes that happened in the parallel rails.
   * Gathers values for each key across ALL buckets and merges them.
   */
  private Flux<Result> gatherResultsFromAllBuckets(BucketedState state) {
    return Flux.defer(() -> {
      // Merge all keys and their value sets from all buckets
      Map<String, Set<String>> mergedResults = new HashMap<>();

      for (int bucket = 0; bucket < PARALLELISM; bucket++) {
        Map<String, Set<String>> bucketData = state.getBucketData(bucket);
        // Merge each bucket's data into the combined result
        for (Map.Entry<String, Set<String>> entry : bucketData.entrySet()) {
          mergedResults
              .computeIfAbsent(entry.getKey(), k -> new HashSet<>())
              .addAll(entry.getValue());
        }
      }

      // Convert merged results to Result objects
      return Flux.fromStream(mergedResults.entrySet().stream().map(entry -> new Result(entry.getKey(), entry.getValue())));
    });
  }

  /**
   * Shared state with per-bucket maps.
   * Each bucket's map is only written to by one parallel rail.
   * No synchronization or volatile keywords used.
   * Values are Sets to accumulate multiple values per key within each bucket.
   */
  static class BucketedState implements AutoCloseable {
    // Array of maps, one per bucket - no volatile, no synchronization
    // Each map holds key -> Set of values
    private final Map<String, Set<String>>[] bucketMaps;

    @SuppressWarnings("unchecked")
    BucketedState(int bucketCount) {
      this.bucketMaps = new HashMap[bucketCount];
      for (int i = 0; i < bucketCount; i++) {
        bucketMaps[i] = new HashMap<>();
      }
    }

    // Called from parallel rails - each bucket only accessed by one rail
    void addToBucket(int bucketId, String key, String value) {
      bucketMaps[bucketId]
          .computeIfAbsent(key, k -> new HashSet<>())
          .add(value);
    }

    // Called from thenMany - needs to see all writes from parallel rails
    Map<String, Set<String>> getBucketData(int bucketId) {
      return bucketMaps[bucketId];
    }

    @Override
    public void close() {
      // cleanup TODO is it really needed?
    }
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
    ParallelMapVisibility prototype = new ParallelMapVisibility();

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
