package victor.training.reactor.workshop;

import org.jooq.lambda.Unchecked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class Errors {
  protected final Logger log = LoggerFactory.getLogger(getClass());

  public Errors(Dependency dependency) {
    this.dependency = dependency;
  }

  final Dependency dependency;

  interface Dependency {
    Mono<String> call();

    Mono<String> backup();

    Mono<Void> sendError(Throwable e);

    Flux<String> downloadLargeData();
  }

  // HINT HINT HINT HINT HINT HINT HINT HINT HINT HINT HINT HINT HINT HINT HINT HINT
  // HINT HINT HINT HINT HINT HINT HINT HINT HINT HINT HINT HINT HINT HINT HINT HINT
  // HINT HINT HINT HINT HINT HINT HINT HINT HINT HINT HINT HINT HINT HINT HINT HINT
  // HINT: Most exercises in this file need operators(=methods) containing the word 'error' :)

  // *** Note: methods returning Mono/Flux should never THROW Exceptions but return them in Mono.error(Ex) ***

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
      log.error("Exception occurred: " + e, e);  //replace this catch with equivalent reactive code. avoid .block()
      throw e;
    }
  }

  // ==================================================================================================

  // TODO Wrap any exception in the call() in a new IllegalStateException("Call failed", originalException).
  public Mono<String> p02_wrap() {
    try {
      return dependency.call();
    } catch (Exception originalException) { // <-- do this on any exception in the future, then delete this USELESS catch
      throw new IllegalStateException(originalException);
    }
  }

  // ==================================================================================================

  // TODO Return "default" if the call fails.
  public Mono<String> p03_defaultValue() {
    try {
      return dependency.call();
    } catch (Exception e) {
      return Mono.just("default"); // <-- do this on any exception in the future, then delete this USELESS catch
    }
  }

  // ==================================================================================================

  // TODO Call dependency#backup() if #call() fails.
  public Mono<String> p04_fallback() {
    try {
      return dependency.call();
    } catch (Exception e) {
      return dependency.backup(); // <-- do this on any exception in the future, then delete this USELESS catch
    }
  }

  // ==================================================================================================

  // TODO Call dependency#sendError(ex) on any exception in the call(), and then let the original error flow to the client
  public Mono<String> p05_sendError() {
    try {
      return dependency.call();
    } catch (Exception e) {
      dependency.sendError(e).block(); // <-- do this on any exception in the future, then delete this USELESS catch
      throw e;
    }
  }

  // ==================================================================================================

  /**
   * === Try-With-Resources (aka cleanup) ===
   * Close the resource (Writer) *after* the future completes.
   */
  public Mono<Void> p06_usingResourceThatNeedsToBeClosed() throws IOException {
    try (Writer writer = new FileWriter("out.txt")) {// <-- make sure you close the writer AFTER the Mono completes
      return dependency.downloadLargeData()
              .doOnNext(Unchecked.consumer(s -> writer.write(s))) // Unchecked.consumer converts any exception into a runtime one
              .then();
    }
  }

}
