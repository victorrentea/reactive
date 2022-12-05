package victor.training.reactor.workshop;

import org.jooq.lambda.Unchecked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;

public class Errors {
  protected final Logger log = LoggerFactory.getLogger(getClass());

  public Errors(Dependency dependency) {
    this.dependency = dependency;
  }

  final Dependency dependency;

  interface Dependency {
    Mono<String> call();
//    Flux<String> unBulion();//1B

    Mono<String> backup();

    Mono<Void> sendError(Throwable e);
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
//    try {
//      String value = dependency.call().block(); // .block() [AVOID in prod] throws any exception in the Mono
//      return Mono.just(value);
//    } catch (Exception e) {
//      log.error("Exception occurred: " + e, e);  //replace this catch with equivalent reactive code. avoid .block()
//      throw e;
//    }
    return dependency.call()
            .doOnError(e -> log.error("Exception occurred: " + e, e));
  }

  // ==================================================================================================

  // TODO Wrap any exception in the call() in a new IllegalStateException("Call failed", originalException).
  public Mono<String> p02_wrap() {
//    try {
//      return dependency.call();
//    } catch (Exception originalException) { // <-- do this on any exception in the future, then delete this USELESS catch
//      throw new IllegalStateException(originalException);
//    }
    return dependency.call().onErrorMap(IllegalStateException::new);
  }

  // ==================================================================================================

  // TODO Return "default" if the call fails.
  public Mono<String> p03_defaultValue() {
//    try {
//      return dependency.call();
//    } catch (Exception e) {
//      return Mono.just("default"); // <-- do this on any exception in the future, then delete this USELESS catch
//    }
    return dependency.call().onErrorReturn("default");
  }

  // ==================================================================================================

  // TODO Call dependency#backup() if call fails.
  public Mono<String> p04_fallback() {
//    try {
//      return dependency.call();
//    } catch (Exception e) {
//      return dependency.backup(); // <-- do this on any exception in the future, then delete this USELESS catch
//    }
    return dependency.call()
//            .doOnError(ex -> {dependency.backup();})
            .onErrorResume(e -> dependency.backup())
            ;
  }

  // ==================================================================================================

  // TODO Call dependency#sendError(ex) on any exception in the call(), and then let the original error flow to the client
  public Mono<String> p05_sendError() {
//    try {
//      return dependency.call();
//    } catch (Exception e) {
//      dependency.sendError(e).block(); // <-- do this on any exception in the future, then delete this USELESS catch
//      throw e;
//    }
    return dependency.call()
            .onErrorResume(error-> dependency.sendError(error)
                    .then(Mono.error(error)))
            ;

  }

  // ==================================================================================================

  /**
   * === Try-With-Resources (aka cleanup) ===
   * Close the resource (Writer) after the future completes.
   */
  public Mono<Void> p06_try_with_resources() throws IOException {
    // si scrierea foarte multor date intr0-un fisier poate bloca threadul curent. (cu SSDuri riscul e mai mic)
    // scriere pe fisier inchizand fisierul la final
    return dependency.call()
            .flatMap(s ->
                    Mono.<Void, Writer>using(
                      () -> new FileWriter("out.txt"), // create resource
                      writer -> Mono.fromRunnable(Unchecked.runnable(() -> writer.write(s))), // use
                      Unchecked.consumer(Writer::close) // close resource
            ));
  }

  public Mono<Void> method() {
    return dependency.call()
            .map(Unchecked.function(s -> Files.writeString(new File("out.txt").toPath(), s))) // Unchecked.consumer converts any exception into a runtime one
            .then();
  }

}
