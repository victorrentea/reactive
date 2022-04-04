package victor.training.reactive.reactor.lite.solved;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import victor.training.reactive.reactor.lite.Part04Transform;
import victor.training.reactive.reactor.lite.domain.User;

public class Part04TransformSolved extends Part04Transform {
   @Override
   public Mono<User> capitalizeOne(Mono<User> mono) {
      return mono.map(user -> new User(
          user.getUsername().toUpperCase(),
          user.getFirstname().toUpperCase(),
          user.getLastname().toUpperCase()
          ));
   }

   @Override
   public Flux<User> capitalizeMany(Flux<User> flux) {
      return flux.map(user -> new User(
          user.getUsername().toUpperCase(),
          user.getFirstname().toUpperCase(),
          user.getLastname().toUpperCase()
      ));
   }

   @Override
   public Flux<User> asyncCapitalizeMany(Flux<User> flux) {
      return flux.flatMap(this::asyncCapitalizeUser);
   }
}
