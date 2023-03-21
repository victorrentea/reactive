package victor.training.reactor.workshop;

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
    // equivalent blocking⛔️ code:
    try {
      String value = dependency.call().block(); // .block() [AVOID in prod] throws any exception in the Mono
      return Mono.just(value);
    } catch (Exception e) {
      log.error("Exception occurred: " + e, e);  // <-- do this in a reactive style
      throw e;
    }
  }

  // ==================================================================================================

  // TODO Wrap any exception in the call() in a new IllegalStateException("Call failed", originalException).
  public Mono<String> p02_wrap() {
    try {
      return dependency.call();
    } catch (Exception originalException) {
      throw new IllegalStateException(originalException); // <-- do this in a reactive style
    }
  }

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

  // TODO Call dependency#backup() if #call() fails.
  public Mono<String> p04_fallback() {
    try {
      return dependency.call();
    } catch (Exception e) {
      return dependency.backup(); // <-- do this in a reactive style
    }
  }

  // ==================================================================================================

  // TODO Call dependency#sendError(ex) on any exception in the call(), and then let the original error flow to the client
  public Mono<String> p05_sendError() {
    try {
      return dependency.call();
    } catch (Exception e) {
      dependency.sendError(e).block(); // <-- do this in a reactive style
      throw e;
    }
  }

  // ==================================================================================================

  // TODO Call dependency#call() again on error, maximum 4 times in total.
  //  If last retry is still error, log it along with the text "SCRAP-LOGS-TAG"
  //  If a call takes more than 50 millis, consider it to be failure and retry.
  // If needed, investingate using .log("above") / .log("below")
  public Mono<String> p06_retryThenLogError() {
    return dependency.call();
  }

  // ==================================================================================================

  // TODO Call dependency#call() again on error, maximum 4 times in total (as above)
  //  but leave 200 millis backoff between the calls.
  public Mono<String> p07_retryWithBackoff() {
    return dependency.call();
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
