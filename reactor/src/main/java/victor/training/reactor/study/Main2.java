package victor.training.reactor.study;

import org.slf4j.MDC;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.util.stream.IntStream;

public class Main2 {
    private static final Logger logger = LoggerFactory.getLogger(Main2.class);
    public static void main(String[] args) {
        Listener listener = new Listener();

        MDC.put("myKey", "myValue");
        logger.info("start");
        Flux.fromStream(IntStream.range(0, 1000)
            .mapToObj(Integer::toString))
            .doOnNext(listener::onEvent)
            .blockLast();
    }

    static class Listener {
        Sinks.Many<String> outputs = Sinks.many().unicast().onBackpressureBuffer();
        void onEvent(String event) {
            outputs.tryEmitNext(event);
        }

        Listener() {
            outputs.asFlux()
                .publishOn(Schedulers.newParallel("listener-", 4))
                .subscribe(logger::info);
        }
    }
}
