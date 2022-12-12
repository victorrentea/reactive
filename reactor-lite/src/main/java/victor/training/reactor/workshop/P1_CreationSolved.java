package victor.training.reactor.workshop;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

import static java.time.Duration.ofMillis;

public class P1_CreationSolved extends P1_Creation {

  public Mono<String> mono1_just() {
    return Mono.just("foo");

    // or, (more complex) using manual signals
    //    return Mono.create(sink -> {
    //      sink.success("foo");
    //    });
  }

  public Mono<String> mono2_empty() {
    return Mono.empty();
  }

  public Mono<String> mono3_optional(String data) {
    return Mono.justOrEmpty(data);
  }

  public Mono<String> mono4_error() {
    return Mono.error(new IllegalStateException());
  }

  public Mono<String> mono5_noSignal() {
    return Mono.never();

    // or (more complex) using manual signals
    //    return Mono.create(sink -> {
    // no signal emitted on sink
    //    });
  }

  public Mono<String> mono6_delayedData() {
    return Mono.just("BOO").delayElement(ofMillis(100));
  }

  public Mono<LocalDateTime> mono7_fromCallable() {
    return Mono.fromCallable(() -> LocalDateTime.now());
  }

  public Mono<Void> mono8_delayedCompletion() {
    return Mono.delay(ofMillis(100)).then();
  }

  public Flux<String> flux1_values() {
    return Flux.just("foo", "bar");
  }

  public Flux<String> flux2_fromList(List<String> list) {
    return Flux.fromIterable(list);
  }

  public Flux<String> flux3_empty() {
    return Flux.empty();
  }

  public Flux<String> flux4_error() {
    return Flux.error(new IllegalStateException());
  }

  public Flux<Long> flux5_delayedElements() {
    return Flux.interval(ofMillis(100)).take(10);
  }

  public Flux<String> logSignals(Flux<String> flux) {
    return flux.log();
  }

  @Override
  public Mono<String> reactorContext_read() {
    return Mono.deferContextual(context -> Mono.just("Hi " + context.get("username")));
  }
}
