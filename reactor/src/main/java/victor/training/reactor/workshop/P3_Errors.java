package victor.training.reactor.workshop;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.jooq.lambda.Unchecked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.Duration;

import static java.time.Duration.ofMillis;
import static reactor.core.publisher.Mono.error;

public class P3_Errors {
  protected final Logger log = LoggerFactory.getLogger(getClass());

  public P3_Errors(Dependency dependency) {
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
    return dependency.call()
        .doOnError(e -> log.error("Exception occurred: " + e, e));
  }

  // ==================================================================================================

  // TODO Wrap any exception in the call() in a new IllegalStateException("Call failed", originalException).
  public Mono<String> p02_wrap() {
    return dependency.call()
        .onErrorMap(e -> new IllegalStateException("Valeu + id " +e, e));
  }
//
//  public Mono<String> call() {
////    throw new ...
//    return Mono.error(new IllegalStateException());
//  }

  // ==================================================================================================

  // TODO Return "default" if the call fails.
  public Mono<String> p03_defaultValue() {
    try {
      return dependency.call();
    } catch (Exception e) {
      return Mono.just("default"); // <-- do this in a reactive style
    }
  }

  // ==================================================================================================

  // TODO Call dependency#backup() if #call() fails. ! both do network!!
  public Mono<String> p04_fallback() {
    return dependency.call()
        .onErrorResume(e -> dependency.backup())
        ;
//        .onErrorContinue()// DARK DARK MAGIC pt streamuri infinite doar eg kafka
//      return dependency.backup(); // <-- do this in a reactive style
  }

  // ==================================================================================================

  // TODO Call dependency#sendError(ex) on any exception in the call(), and then let the original error flow to the client
  public Mono<String> p05_sendError() {
//    try {
//      return dependency.call();
//    } catch (Exception e) {
//      dependency.sendError(e).block(); // <-- do this in a reactive style
//      throw e;
//    }
    return dependency.call()
        .onErrorResume(e -> dependency.sendError(e).then(error(e)));
  }

  // ==================================================================================================

  // TODO Call dependency#call() again on error, maximum 4 times in total.
  //  If last retry is still error, log it along with the text "SCRAP-LOGS-TAG"
  //  If a call takes more than 50 millis, consider it to be failure and retry.
  // If needed, investingate using .log("above") / .log("below")

  public Mono<String> p06_retryThenLogError() {
    return dependency.call()
        .timeout(ofMillis(50))
        .retry(3)
        .doOnError(e -> log.error("SCRAP-LOGS-TAG valeu " + e, e));
  }

  // ai face on call support 1/2 zile pt +50% sa sal daca sunt 1/luni in medie

  // ==================================================================================================

  // TODO Call dependency#call() again on error, maximum 4 times in total (as above)
  //  but leave 200 millis backoff between the calls.
//  @CircuitBreaker()
  public Mono<String> p07_retryWithBackoff() {
    return dependency.call()
        .retryWhen(Retry.backoff(4, ofMillis(200)));
    // mai profy pt retryuri poti plugin-a resilience4j (ala de ofera si @Retry)
  }

  // ==================================================================================================

  /**
   * === Try-With-Resources (aka cleanup) ===
   * Close the resource (Writer) *after* the future completes.
   */
  public Mono<Void> p08_usingResourceThatNeedsToBeClosed() throws IOException {
    try (Writer writer = new FileWriter("out.txt")) {// <-- make sure you close the writer AFTER the Mono completes
      return dependency.downloadManyElements()
              .doOnNext(Unchecked.consumer(s -> writer.write(s))) // Unchecked.consumer converts any exception into a runtime one
              .then();
    }
  }

}
