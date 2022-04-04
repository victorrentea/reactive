package victor.training.reactive.reactor.lite.solved;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import victor.training.reactive.reactor.lite.Part04Transform;
import victor.training.reactive.reactor.lite.Part05Merge;
import victor.training.reactive.reactor.lite.domain.User;

public class Part05MergeSolved extends Part05Merge {
   @Override
   public Flux<User> mergeFluxWithInterleave(Flux<User> flux1, Flux<User> flux2) {
      return flux1.mergeWith(flux2);
   }

   @Override
   public Flux<User> mergeFluxWithNoInterleave(Flux<User> flux1, Flux<User> flux2) {
      return flux1.concatWith(flux2);
   }

   @Override
   public Flux<User> createFluxFromMultipleMono(Mono<User> mono1, Mono<User> mono2) {
      return Flux.concat(mono1, mono2);
   }
}
