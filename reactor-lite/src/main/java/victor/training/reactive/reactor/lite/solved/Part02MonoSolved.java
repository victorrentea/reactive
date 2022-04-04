package victor.training.reactive.reactor.lite.solved;

import reactor.core.publisher.Mono;
import victor.training.reactive.reactor.lite.Part02Mono;

public class Part02MonoSolved extends Part02Mono {
   @Override
   public Mono<String> emptyMono() {
      return Mono.empty();
   }

   @Override
   public Mono<String> monoWithNoSignal() {
//      return Mono.never();
      // or (more complex) using manual signals
      return Mono.create(sink -> {
         // no singlal emitted
      });
   }

   @Override
   public Mono<String> fooMono() {
//      return Mono.just("foo");
      // or, (more complex) using manual signals
      return Mono.create(sink -> {
         sink.success("foo");
      });
   }

   @Override
   public Mono<String> optionalMono(String data) {
      return Mono.justOrEmpty(data);
   }

   @Override
   public Mono<String> errorMono() {
      return Mono.error(new IllegalStateException());
   }
}
