package victor.training.reactor.workshop;

import org.jooq.lambda.Unchecked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
  }

  // HINT HINT HINT HINT HINT HINT HINT HINT HINT HINT HINT HINT HINT HINT HINT HINT
  // HINT HINT HINT HINT HINT HINT HINT HINT HINT HINT HINT HINT HINT HINT HINT HINT
  // HINT HINT HINT HINT HINT HINT HINT HINT HINT HINT HINT HINT HINT HINT HINT HINT

  // MOST tasks in this file need various operators(=methods) containing the word 'error' :)

  // ==================================================================================================

  /**
   * Log any exception from the Mono, and rethrow the same error.
   */
  public Mono<String> p01_log_rethrow() {
    // equivalent blocking⛔️ code: in reactive code methods returning Mono/Flux never THROW Ex but return Mono.error(Ex)
    try {
      String value = dependency.call().block();
      return Mono.just(value);
    } catch (Exception e) {
      log.error("Exception occurred: " + e, e);  //replace this catch with equivalent reactive code. avoid .block()
      throw e;
    }
  }

  // ==================================================================================================

  /**
   * Wrap any exception in the Mono from call() in a new IllegalStateException("Call failed", originalException).
   */
  public Mono<String> p02_wrap() {
    // blocking⛔️ code: replace with equivalent reactive code; remove .block()
    try {
      return Mono.just(dependency.call().block());
    } catch (Exception originalException) {
      throw new IllegalStateException(originalException);
    }
  }

  // ==================================================================================================

  /**
   * Return "default" on any exception in the future.
   */
  public Mono<String> p03_defaultValue() {
    try {
      return dependency.call();
    } catch (Exception e) {
      return Mono.just("default"); // <-- do this on any exception in the future, then delete this USELESS catch
    }
  }

  // ==================================================================================================

  /**
   * Call dependency#backup() on any exception in the first Mono.
   */
  public Mono<String> p04_fallback() {
    try {
      return dependency.call();
    } catch (Exception e) {
      return dependency.backup(); // <-- do this on any exception in the future, then delete this USELESS catch
    }
  }

  // ==================================================================================================

  /**
   * Call dependency#sendError(ex) on any exception in the first Mono, and let the error flow to the client
   */
  public Mono<String> p05_sendError() {
    try {
      return dependency.call();
    } catch (Exception e) {
      dependency.backup().block(); // <-- do this on any exception in the future, then delete this USELESS catch
      throw e;
    }
  }

  // ==================================================================================================

  /**
   * === Try-With-Resources (aka cleanup) ===
   * Close the resource (Writer) after the future completes.
   */
  public Mono<Void> p06_try_with_resources() throws IOException {
    try (Writer writer = new FileWriter("out.txt")) {// <-- make sure you close the writer AFTER the Mono completes
      return dependency.call()
              .map(s -> Unchecked.runnable(() -> writer.write(s))) // Unchecked.consumer converts any exception into a runtime one
              .then();
    }
  }

}
