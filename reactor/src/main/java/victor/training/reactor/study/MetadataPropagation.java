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
    ContextRegistry.getInstance().registerThreadLocalAccessor(  /*from io.micrometer:context-propagation:1.1.3*/
        "mdc",
        () -> {
//          log.info("AHA!");
          return MDC.getCopyOfContextMap();
        },      // capture
        m -> { if (m == null) MDC.clear(); else MDC.setContextMap(m); }, // restore
        MDC::clear                     // cleanup
    );
    Hooks.enableAutomaticContextPropagation(); // reactor 3.7

    MDC.put("myKey", "myValue");
    log.info("in main thread: " + MDC.get("myKey"));
    Flux.just("1")
        .doOnNext(listener::onEvent)
//        .contextCapture() // not needed if using Hooks.enableAutomaticContextPropagation()
        .subscribeOn(Schedulers.boundedElastic())
        .blockLast(); // runs all the work in main as there's no delay operator or network netty call
  }
  static class Listener {
    Sinks.Many<String> outputs = Sinks.many().unicast().onBackpressureBuffer();

    Listener() { // running in main() before⚠️ MDC is set
      log.info("Listener constructor: " + MDC.get("myKey"));
      outputs.asFlux()
          .doOnNext(e -> log.info(".doOnNext: " + MDC.get("myKey")))
          .publishOn(Schedulers.newParallel("listener2-", 4)) // move to a new thread
          .doOnNext(e -> log.info(".doOnNext AFTER publishOn: " + MDC.get("myKey")))
          .subscribe(msg -> log.info(".subscribe: " + MDC.get("myKey")));
    }

    void onEvent(String event) {
      log.info("onEvent call: " + MDC.get("myKey"));
      MDC.put("myKey", "myValue-modified-in-onEvent"); // bad practice to modify MDC in such method
      outputs.tryEmitNext(event); // copies current THreadLocal MDC into the emitted element's reactor context
      log.info("After emit");
    }
  }
}
