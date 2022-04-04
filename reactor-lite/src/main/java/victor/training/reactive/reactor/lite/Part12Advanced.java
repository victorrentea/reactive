package victor.training.reactive.reactor.lite;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
public class Part12Advanced {

   // TODO Create a Flux returning 3 different random numbers to anyone subscribing to it.
   // Note: multiple subscribers will subscribe!
   // Use generateRandomInts()!
   public Flux<Integer> defer() {
      return null;
   }

   protected List<Integer> generate3RandomInts() {
      Random r = new Random(); // depends on current time
      return r.ints(3).boxed().collect(Collectors.toList());
   }
   //========================================================================================

   // TODO emit a number sequence (0,1,2,3...) every 100ms *even without a registered subscriber*
   // (the emissions should start immediately when hotPublisher() is called)
   // Hint: Flux.publish(): ConnectableFlux
   // Brain: connect(): Disposable > how to handle?
   public Flux<Long> hotPublisher() {
      return null;
   }

   //========================================================================================

   // TODO Emit a word at every onNext signal in timeFlux.
   // When words are done, complete the flux.
   // Late subscribers should receive words from the start.
   public Flux<String> replay(Flux<Long> timeFlux, String text) {
      String[] words = text.split("\\s");
      return null;
   }

   //========================================================================================

   // TODO call the fetchData function on Schedulers#boundedElastic and return the value + the System.currentTimeMillis()
   //  Note: that you aren't allowed to call fetchData more than once, no matter how many subscribe
   //  Note: fetchData should be called for the first subscribe
   //  Note: the timestamp should be new for every subscribe
   public Mono<String> share(Supplier<String> fetchData) {
      return Mono.fromSupplier(fetchData)
          .doOnSubscribe(s -> log.info("Subscribing now"))
          ;
   }

   //========================================================================================

   // TODO The returned Mono emits after 1 second "Hello " + the current username got from Reactor Context
   // Hint: Mono.deferContextual allows access to context propagated from downstream
   public Mono<String> reactorContext() {
      return null;
   }

   //========================================================================================
}
