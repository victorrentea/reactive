package victor.training.reactor.workshop;

import org.jooq.lambda.Unchecked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;


public class ErrorsSolved extends Errors {
    private static final Logger log = LoggerFactory.getLogger(ErrorsSolved.class);

    public ErrorsSolved(Dependency dependency) {
        super(dependency);
    }

    public Mono<String> p01_log_rethrow() {
        return dependency.call()
                .doOnError(e -> log.error("Exception occurred: " + e, e));
    }

    public Mono<String> p02_wrap() {
      return dependency.call().onErrorResume(e -> Mono.error(new IllegalStateException("Call failed", e)));
    }

    public Mono<String> p03_defaultValue() {
        return dependency.call().onErrorReturn("default");
    }

    public Mono<String> p04_fallback() {
        return dependency.call().onErrorResume(e -> dependency.backup());
    }

    public Mono<String> p05_sendError() {
        return dependency.call().onErrorResume(e -> dependency.sendError(e).then(Mono.error(e)));
    }

    public Mono<Void> p06_try_with_resources() throws IOException {
      return dependency.call()
              .flatMap(s -> Mono.<Void, Writer>using(
                      () -> new FileWriter("out.txt"), // create resource
                      writer -> Mono.fromRunnable(Unchecked.runnable(() -> writer.write(s))), // use
                      Unchecked.consumer(Writer::close) // close resource
              ))
              ;
    }
}
