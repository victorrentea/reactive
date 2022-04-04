package victor.training.reactive.reactor.lite.solved;

import reactor.core.publisher.Flux;
import victor.training.reactive.reactor.lite.Part01Flux;

import java.time.Duration;
import java.util.List;

public class Part01FluxSolved extends Part01Flux {
   @Override
   public Flux<String> emptyFlux() {
      return Flux.empty();
   }

   @Override
   public Flux<String> fooBarFluxFromValues() {
      return Flux.just("foo", "bar");
   }

   @Override
   public Flux<String> fluxFromList(List<String> list) {
      return Flux.fromIterable(list);
   }

   @Override
   public Flux<String> errorFlux() {
      return Flux.error(new IllegalStateException());
   }

   @Override
   public Flux<Long> countEach100ms() {
      return Flux.interval(Duration.ofMillis(100)).take(10);
   }
}
