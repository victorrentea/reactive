package victor.training.reactor.workshop;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

import static java.time.Duration.ofMillis;

public class BasicsSolved extends Basics {
  public Mono<String> emptyMono() {
    return Mono.empty();
  }

  public Mono<String> monoWithNoSignal() {
    return Mono.never();

    // or (more complex) using manual signals
    //    return Mono.create(sink -> {
    // no signal emitted on sink
    //    });
  }

  public Mono<String> fooMono() {
    return Mono.just("foo");

    // or, (more complex) using manual signals
    //    return Mono.create(sink -> {
    //      sink.success("foo");
    //    });
  }

  public Mono<String> optionalMono(String data) {
    return Mono.justOrEmpty(data);
  }

  public Mono<String> errorMono() {
    return Mono.error(new IllegalStateException());
  }

  public Mono<Void> delayedCompletion() {
    return Mono.delay(ofMillis(100)).then();
  }

  public Flux<String> emptyFlux() {
    return Flux.empty();
  }

  public Flux<String> fooBarFluxFromValues() {
    return Flux.just("foo", "bar");
  }

  public Flux<String> fluxFromList(List<String> list) {
    return Flux.fromIterable(list);
  }

  public Flux<String> errorFlux() {
    return Flux.error(new IllegalStateException());
  }

  public Flux<Long> countEach100ms() {
    return Flux.interval(ofMillis(100)).take(10);
  }

}
