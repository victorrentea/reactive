package victor.training.reactor.workshop;

import org.jooq.lambda.Unchecked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static java.time.Duration.ofMillis;
import static reactor.util.retry.Retry.*;

public class C3_Errors {
  protected final Logger log = LoggerFactory.getLogger(getClass());

  public C3_Errors(Dependency dependency) {
    this.dependency = dependency;
  }

  protected final Dependency dependency;

  protected interface Dependency {
    Mono<String> call();

    Mono<String> backup();

    Mono<Void> sendError(Throwable e);

    Flux<String> downloadManyElements();
  }

  // HINT 😉: Most solutions involve reactive operators (methods) containing the word 'error'

  // *** Note: methods returning Mono/Flux should never THROW an Exception.
  //  Insted, it should return the exception in Mono.error(Ex) ***

  // ==================================================================================================

  /**
   * TODO Log any exception from the Mono, and rethrow the same error.
   */
  public Mono<String> p01_log_rethrow() {
    // equivalent blocking⛔️ code:
//    try {
//      String value = dependency.call().block(); // .block() [AVOID in prod] throws any exception in the Mono
//      return Mono.just(value);
//    } catch (Exception e) {
//      log.error("Exception occurred: " + e, e);  // <-- do this in a reactive style
//      throw e;
//    }
    return dependency.call()
            .doOnError(e -> log.error("Exception occurred: " + /*Objects.requireNonNull(null) +*/ e, e));
        // .doOn.... to perform in-memory side effects (no IO)
  }

  // ==================================================================================================

  // TODO Wrap any exception in the call() in a new IllegalStateException("Call failed", originalException).
  public Mono<String> p02_wrap() {
//    try {
//      return dependency.call();
//    } catch (Exception originalException) {
//      throw new IllegalStateException(originalException); // <-- do this in a reactive style
//    }
    return dependency.call()
        .onErrorMap(IllegalStateException::new);
  }

  // ==================================================================================================

  // TODO Return "default" if the call fails.
  public Mono<String> p03_defaultValue() {
//    try {
//      return dependency.call();
//    } catch (Exception e) {
//      return Mono.just("default"); // <-- do this in a reactive style
//    }
    return dependency.call()
        .onErrorReturn("default");
  }

  // ==================================================================================================

  // TODO Call dependency#backup() if #call() fails.
  public Mono<String> p04_fallback() {
//    try {
//      return dependency.call(); // call flaky but exact system
//    } catch (Exception e) {
//      return dependency.backup(); // <-- do this in a reactive style; fallback to a less precise but stable
//    }
    return dependency.call()
        .onErrorResume(e -> dependency.backup());
  }

  // ==================================================================================================
  // TODO Call dependency#sendError(ex) on any exception in the call(),
  //  and then let the original error flow to the client
  public Mono<String> p05_sendError() {

//    try {
//      return dependency.call();
//    } catch (Exception e) {
//      dependency.sendError(e).block(); // <-- do this in a reactive style
//      // when payment fails, call notification-service to tell the user that payment failed
//      throw e;
//    }\

//    Flux<String> fromADB;
//    fromADB.subscribe(e->api.send(e));

    return dependency.call()
        .delaySubscription(ofMillis(5))
//        .publishOn(Schedulers.newBoundedElastic(20, 100, "my-hibernate-pool"))
//        .publishOn(Schedulers.newBoundedElastic(Integer.MAX_VALUE, 0, Thread::ofVirtual,"my-hibernate-pool"))
//        .flatMap

//    https://projectreactor.io/docs/core/release/reference/coreFeatures/schedulers.html?utm_source=chatgpt.com
        .doOnError(e-> log.error("What thread am I running on?"))
        // runs on parallel-1 (ot of max #CPUs=10)
        // tomcat had 200 threads default
//        .doOnError(e -> dependency.sendError(e).block()); // ❌ don't block in src/main; blocked 10% of parallel scheduler (aka thread pool)
//        .doOnError(e -> dependency.sendError(e)); // ❌ NOTHING HAPPENS if you DON'T SUBSCRIBE ™️; no call is made2

//        .onErrorResume(e->dependency.sendError(e).then(Mono.error(e))); // ⭐️

        .doOnError(e -> dependency.sendError(e).subscribe());
        // aka fire-and-forget vs ⭐️
        // ± don't know if/when the action happened ⚠️any errors❌?
        // - Broken-Chain effect: you don't propagate the ReactorContext metadata: security, OTEL traceId...
        //      ie. sendError can't use any parent JWT AccessToken, won't propagate TraceID on that call
        // ± cancellation of outer chain does not propagate in the fire-and-forget
        // - backpressure
        // ~ CompletableFuture.runAsync(()->backroundWork());

        // Reactor COntext propagation
//        .doOnEach(signal -> {
//          if (signal.isOnError()) {
//              dependency.sendError(signal.getThrowable())
//                  .contextWrite(signal.getContextView())
//                  .subscribe();
//          }
//        });
  }

  // ==================================================================================================

  // TODO Call dependency#call() again on error, maximum 4 times in total.
  //  If last retry is still error, log it along with the text "SCRAP-LOGS-TAG"
  //  If a call takes more than 50 millis, consider it to be failure and retry.
  // If needed, investingate using .log("above") / .log("below")
  public Mono<String> p06_retryThenLogError() {
    return dependency.call()
        .log("before timeout")
        .timeout(ofMillis(50))
        .log("after timeout")

        // signal hook do to non-blocking stuff (typically logging)
        .doOnError(e->log.error("x"))
          //does not change any signal.
          //reactor context is not changed/used

        // handling the error: replacing, retrying,wrapping
//        .onErrorResume(mono)
//        .onErrorReturn(value)

        .retry(3)
//        .timeout(Duration.ofMillis(150)) // 150ms here for ALL THE RETRIES SUM
        .doOnError(e -> log.error("SCRAP-LOGS-TAG " + e))
        ;
  }






  // ==================================================================================================

  // TODO Call dependency#call() again on error, maximum 4 times in total (as above)
  //  but leave 200 millis backoff between the calls.
  public Mono<String> p07_retryWithBackoff() {
//    Bulkhead bulkhead = Bulkhead.ofDefaults("name").maxConcurrency(3);
    // from resilience4j https://resilience4j.readme.io/docs/examples-1
    return dependency.call()
//        .log()
        .doOnSubscribe(s-> log.info("CALL"))
        .retryWhen(backoff(3, ofMillis(200)).maxBackoff(ofMillis(200)))
        ;
  }

  // ==================================================================================================

  /**
   * === Try-With-Resources (aka cleanup) ===
   * Close the resource (Writer) *after* the future completes.
   */
  public Mono<Void> p08_usingResourceThatNeedsToBeClosed() throws IOException {
//    try (Writer writer = new FileWriter("out.txt")) {// <-- make sure you close the writer AFTER the Mono completes
//      return dependency.downloadManyElements()
//              .doOnNext(Unchecked.consumer(s -> writer.write(s))) // Unchecked.consumer converts any exception into a runtime one
//              .then();
//    }
    return Mono.using( //~ try-with-resources
        // produce the resource
        () -> new FileWriter("out.txt"),

        // act with it
        writer -> dependency.downloadManyElements() // network download "GET"

            .doOnNext(Unchecked.consumer(s -> writer.write(s)))
            // what if writing throw an exception?

//            .onErrorContinue()
            // lack of backpressure could become a problem if writing is slower than downloading = NOT LIKELY
            // to enable backpressure if I were sending to another system (eg. Kafka), I'd use .flatMap(.., concurrency: X)
            .then(),

        // close it
        Unchecked.consumer(Writer::close)
    );
  }

}
