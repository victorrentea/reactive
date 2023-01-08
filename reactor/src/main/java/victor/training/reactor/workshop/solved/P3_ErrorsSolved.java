package victor.training.reactor.workshop.solved;

import org.jooq.lambda.Unchecked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import victor.training.reactor.workshop.P3_Errors;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.Duration;


public class P3_ErrorsSolved extends P3_Errors {
    private static final Logger log = LoggerFactory.getLogger(P3_ErrorsSolved.class);

    public P3_ErrorsSolved(Dependency dependency) {
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

    public Mono<String> p06_retryThenLogError() {
        return dependency.call()
                .timeout(Duration.ofMillis(200))
                .log("above")
                .retry(3)
                .log("below")
                .doOnError(e -> log.error("Final fail (SCRAP LOGS FOR ME): " + e));
    }

    public Mono<String> p07_retryWithBackoff() {
        return dependency.call()
                .retryWhen(Retry.backoff(3, Duration.ofMillis(200)));
    }

    public Mono<Void> p08_usingResourceThatNeedsToBeClosed() throws IOException {
      return Mono.<Void, Writer>using(

              // create resource:
              () -> new FileWriter("out.txt"),

              // use resource:
              writer -> dependency.downloadManyElements()
                      .doOnNext(Unchecked.consumer(s -> writer.write(s)))
                      .then(),

              // close resource
              Unchecked.consumer(Writer::close)
          );
    }
}
