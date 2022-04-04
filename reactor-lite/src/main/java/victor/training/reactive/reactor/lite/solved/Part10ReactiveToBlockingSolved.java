package victor.training.reactive.reactor.lite.solved;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import victor.training.reactive.reactor.lite.Part10ReactiveToBlocking;
import victor.training.reactive.reactor.lite.domain.User;

public class Part10ReactiveToBlockingSolved extends Part10ReactiveToBlocking {
   @Override
   public User monoToValue(Mono<User> mono) {
      return mono.block();
   }

   @Override
   public Iterable<User> fluxToValues(Flux<User> flux) {
      return flux.toIterable();
   }
}
