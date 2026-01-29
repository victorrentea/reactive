package victor.training.reactor.study;

import io.micrometer.context.ContextRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.util.stream.IntStream;

public class MetadataPropagation {
  private static final Logger log = LoggerFactory.getLogger(MetadataPropagation.class);

  public static void main(String[] args) {
    Listener listener = new Listener();
    /*from io.micrometer:context-propagation*/
    ContextRegistry.getInstance().registerThreadLocalAccessor(
        "mdc",
        MDC::getCopyOfContextMap,      // capture
        m -> { if (m == null) MDC.clear(); else MDC.setContextMap(m); }, // restore
        MDC::clear                     // cleanup
    );

    Hooks.enableAutomaticContextPropagation();

    MDC.put("myKey", "myValue");
    log.info("in main thread: " + MDC.get("myKey"));
    Flux.fromStream(IntStream.range(0, 10)
            .mapToObj(Integer::toString))
        .doOnNext(listener::onEvent)
//        .contextCapture() // only needed if Hooks.enableAutomaticContextPropagation() is not used
        .blockLast();
  }

  static class Listener {
    Sinks.Many<String> outputs = Sinks.many().unicast().onBackpressureBuffer();

    void onEvent(String event) {
      log.info("onEvent: " + MDC.get("myKey"));
      outputs.tryEmitNext(event);
    }

    Listener() {
      outputs.asFlux()
          .doOnNext(e -> log.info("doOnNext : " + MDC.get("myKey")))
          .publishOn(Schedulers.newParallel("listener-", 4))
//          .subscribeOn(Schedulers.newParallel("listener-", 4))
          .doOnNext(e -> log.info("doOnNext AFTER publishOn: " + MDC.get("myKey")))
//          .contextCapture()
          .subscribe(msg -> log.info(".subscribe: " + MDC.get("myKey")));
    }
  }
}
