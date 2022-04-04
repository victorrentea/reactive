package victor.training.reactive.reactor.lite.solved;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import victor.training.reactive.reactor.lite.Part12Advanced;

import java.time.Duration;
import java.util.function.Supplier;

import static reactor.core.scheduler.Schedulers.boundedElastic;

@Slf4j
public class Part12AdvancedSolved extends Part12Advanced {
   @Override
   public Flux<Integer> defer() {
      return Flux.defer(() -> Flux.fromIterable(generate3RandomInts()));
   }

   @Override
   public Flux<Long> hotPublisher() {
      ConnectableFlux<Long> flux = Flux.interval(Duration.ofMillis(100)).publish();
      flux.connect();
      return flux;
   }

   @Override
   public Flux<String> replay(Flux<Long> timeFlux, String text) {
      ConnectableFlux<String> replay = Flux.fromArray(text.split("\\s"))
          .log("before zip")
          .zipWith(timeFlux, (word, tick) -> word)
          .log("After zip")
          .replay();
      replay.connect();
      return replay;
   }

   @Override
   public Mono<String> share(Supplier<String> fetchData) {
      return Mono.fromSupplier(fetchData)
          .subscribeOn(boundedElastic())
          .cache()
          .map(s -> s + System.currentTimeMillis())
          .doOnSubscribe(s -> log.info("Subscribing now"));
   }

   @Override
   public Mono<String> reactorContext() {
      return Mono.deferContextual(context -> Mono.just("Hello " + context.get("username")))
          .delayElement(Duration.ofSeconds(1));
   }
}
