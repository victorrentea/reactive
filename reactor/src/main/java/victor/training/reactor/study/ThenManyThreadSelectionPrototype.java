package victor.training.reactor.study;

import org.xmlunit.builder.Input;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;

/**
 * PROTOTYPE CODE FOR DISCUSSION PURPOSES ONLY - NOT PRODUCTION CODE
 * <p>
 * This prototype demonstrates the thread selection question with thenMany():
 * <p>
 * QUESTION 1: With thenMany() operator, how does it determine which thread is used
 * to run its action when it follows parallel() and flatMap()?
 * It seems like it uses one of the parallel threads from the Scheduler on which
 * those parallel threads are running.
 * <p>
 * QUESTION 2: It seems like thenMany often chooses the same thread to run the action
 * across multiple concurrent operations (each with their own Flux), which results
 * in serialization of those thenMany actions. What is the best way to ameliorate this?
 */
public class ThenManyThreadSelectionPrototype {

  private static final int PARALLELISM = 4;
  private static final int PREFETCH = 20;
  private static final Random RANDOM = new Random();

  // Shared scheduler across multiple concurrent Flux operations, ~ prod
  private final Scheduler sharedParallelScheduler =
      Schedulers.newParallel("shared-worker", PARALLELISM);

  /**
   * This method creates a Flux that:
   * 1. Processes items in parallel using the shared scheduler
   * 2. Then runs a potentially expensive operation in thenMany()
   * <p>
   * When multiple callers invoke this method concurrently, each gets their own Flux,
   * but they all share the same sharedParallelScheduler.
   * <p>
   * OBSERVED BEHAVIOR: The thenMany() action often runs on the same thread
   * (e.g., "shared-worker-1") across different concurrent Flux executions,
   * causing serialization.
   */
  public Flux<Output> process(Flux<Input> items, String operationId) {
    return items
        .parallel(PARALLELISM, PREFETCH)
        .runOn(sharedParallelScheduler, PREFETCH)
        .groups()
        .flatMap(groupedFlux -> {
              int railId = groupedFlux.key();

              return groupedFlux
                  .doOnNext(input -> {
                    // Parallel processing work
                    logThread("Parallel CPU work", operationId, railId);
                    doSomeWork(input);
                  })
                  .doOnComplete(() -> {
                    logThread("Rail complete", operationId, railId);
                  });
            },
            PARALLELISM)
        // After parallel completes, thenMany runs
        // QUESTION: Which thread runs this? Often seems to be the same one
        // across concurrent operations, causing serialization
        .thenMany(runExpensiveGatherOperation(operationId));
  }

  /**
   * This is the expensive operation that runs after parallel processing.
   * When multiple concurrent Flux operations reach this point, we observe
   * that they often serialize on the same worker thread.
   */
  private Flux<Output> runExpensiveGatherOperation(String operationId) {
    return Flux.defer(() -> {
      long startTime = System.currentTimeMillis();
      logThread("thenMany gather operation START", operationId, -1);

      // Simulate expensive work that we want to parallelize across
      // multiple concurrent Flux operations
      Iterator<Output> expensiveIterator = createExpensiveIterator(operationId);

      long elapsed = System.currentTimeMillis() - startTime;
      logThread("thenMany gather operation END (took " + elapsed + "ms)", operationId, -1);

      return Flux.fromIterable(() -> expensiveIterator);
    });
  }

  /**
   * Alternative approach to potentially ameliorate the serialization issue:
   * Use subscribeOn to force thenMany onto a different scheduler
   */
  public Flux<Output> processWithSubscribeOnWorkaround(Flux<Input> items, String operationId) {
    // Separate scheduler for gather operations
    Scheduler gatherScheduler = Schedulers.newParallel("gather-worker", PARALLELISM);

    return items
        .parallel(PARALLELISM, PREFETCH)
        .runOn(sharedParallelScheduler, PREFETCH)
        .groups()
        .flatMap(
            groupedFlux -> groupedFlux.doOnNext(input -> doSomeWork(input)),
            PARALLELISM)
        .thenMany(
            Flux.defer(() -> {
              logThread("thenMany with subscribeOn", operationId, -1);
              return Flux.fromIterable(() -> createExpensiveIterator(operationId));
            }).subscribeOn(gatherScheduler)  // Force onto different scheduler
        );
  }

  /**
   * Another alternative: Use publishOn before thenMany
   */
  public Flux<Output> processWithPublishOnWorkaround(Flux<Input> items, String operationId) {
    Scheduler gatherScheduler = Schedulers.boundedElastic();

    return items
        .parallel(PARALLELISM, PREFETCH)
        .runOn(sharedParallelScheduler, PREFETCH)
        .groups()
        .flatMap(
            groupedFlux -> groupedFlux.doOnNext(input -> doSomeWork(input)),
            PARALLELISM)
        .thenMany(
            runExpensiveGatherOperation(operationId)
                .publishOn(gatherScheduler)  // Switch execution context
        );
  }

  private void logThread(String phase, String operationId, int railId) {
    System.out.printf("[%s] %s - operation=%s, rail=%d%n",
        Thread.currentThread().getName(), phase, operationId, railId);
  }

  private void doSomeWork(Input input) {
    // Simulate work with random duration (5-50ms)
    try {
      int sleepTime = 5 + RANDOM.nextInt(46);
      Thread.sleep(sleepTime);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private Iterator<Output> createExpensiveIterator(String operationId) {
    // Simulate expensive iterator creation - this takes real time!
    // This represents the work done in the gather phase after parallel processing
    List<Output> results = new ArrayList<>();

    // Simulate expensive computation - random 200-500ms
    int expensiveWorkTime = 200 + RANDOM.nextInt(301);
    try {
      Thread.sleep(expensiveWorkTime);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    // Generate some results
    for (int i = 0; i < 5; i++) {
      results.add(new Output(operationId, "result-" + i));
    }

    return results.iterator();
  }

  // Simple data classes
  record Input(String data) {
  }

  record Output(String operationId, String data) {
  }

  /**
   * Demonstrates the serialization problem with multiple concurrent operations
   */
  public static void main(String[] args) throws InterruptedException {
    ThenManyThreadSelectionPrototype prototype = new ThenManyThreadSelectionPrototype();

    System.out.println("=== ThenMany Thread Selection Prototype ===");
    System.out.println("This demonstrates how thenMany() selects threads after parallel/flatMap");
    System.out.println("Watch for: Which thread runs each thenMany gather operation?");
    System.out.println("If the same thread (e.g., shared-worker-1) runs multiple thenMany operations,");
    System.out.println("those operations will serialize, even though they are independent Flux pipelines.");
    System.out.println();
    System.out.println("Starting " + 3 + " concurrent operations, each with its own Flux...");
    System.out.println("---");

    // Create more substantial test data for each operation
    Flux<Input> items1 = Flux.fromStream(
        IntStream.range(0, 20).mapToObj(i -> new Input("op1-item-" + i)));
    Flux<Input> items2 = Flux.fromStream(
        IntStream.range(0, 20).mapToObj(i -> new Input("op2-item-" + i)));
    Flux<Input> items3 = Flux.fromStream(
        IntStream.range(0, 20).mapToObj(i -> new Input("op3-item-" + i)));

    // Use CountDownLatch to wait for all operations to complete
    CountDownLatch latch = new CountDownLatch(3);
    long startTime = System.currentTimeMillis();

    // Subscribe to all three concurrently
    prototype.process(items1, "op-1")
        .doOnComplete(() -> {
          System.out.println(">>> op-1 COMPLETE");
          latch.countDown();
        })
        .subscribe();

    prototype.process(items2, "op-2")
        .doOnComplete(() -> {
          System.out.println(">>> op-2 COMPLETE");
          latch.countDown();
        })
        .subscribe();

    prototype.process(items3, "op-3")
        .doOnComplete(() -> {
          System.out.println(">>> op-3 COMPLETE");
          latch.countDown();
        })
        .subscribe();

    // Wait for all to complete
    latch.await();
    long totalTime = System.currentTimeMillis() - startTime;

    System.out.println("---");
    System.out.println("Total time for all 3 operations: " + totalTime + "ms");

    // Dispose the scheduler to allow JVM to exit
    prototype.sharedParallelScheduler.dispose();
  }
}
