package victor.training.reactor.study;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.*;
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
  private static final Logger log = LoggerFactory.getLogger(ThenManyThreadSelectionPrototype.class);

  // Shared scheduler across multiple concurrent Flux operations, ~ prod
  private final Scheduler sharedParallelScheduler =
      Schedulers.newParallel("shared-worker", PARALLELISM);

  private static void logRail(String message, String operationId, Integer railId) {
    log.info("{}, rail={}: {}", operationId, railId, message);
  }

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
    logRail("Starting process()", operationId, null);
    return items
        .parallel(PARALLELISM, PREFETCH)
        .runOn(sharedParallelScheduler, PREFETCH)
        .groups()
        .flatMap(groupedFlux -> groupedFlux
            .doOnNext(input -> {
              doSomeWork(input,operationId, groupedFlux.key());
            })
            .doOnComplete(() -> logRail("Rail complete", operationId, groupedFlux.key())),
            PARALLELISM)
        // After parallel completes, thenMany runs
        // QUESTION: Which thread runs this? Often seems to be the same one
        // across concurrent operations, causing serialization
        .thenMany(runExpensiveGatherOperation(operationId)
//            .subscribeOn(gatherScheduler) // ✅Recommended FIX
        )
        .doOnComplete(() -> logRail("process() COMPLETE", operationId, null))
        ;
  }

  Scheduler gatherScheduler = Schedulers.newParallel("gather-worker", PARALLELISM);



  private void doSomeWork(Input input, String operationId, Integer railId) {
    try {
      int sleepTime = 5 + RANDOM.nextInt(96);
      logRail("Parallel CPU work taking ms="+sleepTime + " on " + input, operationId, railId);
      Thread.sleep(sleepTime);
      logRail("Parallel CPU work DONE", operationId, railId);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * This is the expensive operation that runs after parallel processing.
   * When multiple concurrent Flux operations reach this point, we observe
   * that they often serialize on the same worker thread.
   */
  private Flux<Output> runExpensiveGatherOperation(String operationId) {
    return Flux.defer(() -> {
      long startTime = System.currentTimeMillis();
      logRail("🟢thenMany gather operation START", operationId, null);

      // Simulate expensive work that we want to parallelize across multiple concurrent Flux chains
      Iterator<Output> expensiveIterator = createExpensiveIterator(operationId);

      long elapsed = System.currentTimeMillis() - startTime;
      logRail("🔴thenMany gather operation END (took " + elapsed + "ms)", operationId, null);
      threadsThatRanThenMany.add(Thread.currentThread().getName());

      return Flux.fromIterable(() -> expensiveIterator);
    });
  }

  LinkedHashSet<String> threadsThatRanThenMany = new LinkedHashSet<>();



  private Iterator<Output> createExpensiveIterator(String operationId) {
    // Simulate expensive iterator creation - this takes real time!
    // This represents the work done in the gather phase after parallel processing
    List<Output> results = new ArrayList<>();

    // Simulate expensive computation - random 200-500ms
    int expensiveWorkTime = 1000+ 200 + RANDOM.nextInt(301);
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
        IntStream.range(0, 10).mapToObj(i -> new Input("op1-item-" + i)));
    Flux<Input> items2 = Flux.fromStream(
        IntStream.range(0, 20).mapToObj(i -> new Input("op2-item-" + i)));
    Flux<Input> items3 = Flux.fromStream(
        IntStream.range(0, 30).mapToObj(i -> new Input("op3-item-" + i)));

    // Use CountDownLatch to wait for all operations to complete
    CountDownLatch latch = new CountDownLatch(3);
    long startTime = System.currentTimeMillis();

    // Subscribe to all three concurrently
    prototype.process(items1, "op-1")
        .doOnComplete(latch::countDown)
        .subscribe();

    prototype.process(items2, "op-2")
        .doOnComplete(latch::countDown)
        .subscribe();

    prototype.process(items3, "op-3")
        .doOnComplete(latch::countDown)
        .subscribe();

    // Wait for all to complete
    latch.await();
    long totalTime = System.currentTimeMillis() - startTime;

    System.out.println("---");
    System.out.println("Total time for all 3 operations: " + totalTime + "ms");
    System.out.println("Threads that ran thenMany gather operations: " + prototype.threadsThatRanThenMany);
    // Dispose the scheduler to allow JVM to exit
    prototype.sharedParallelScheduler.dispose();
  }
}
