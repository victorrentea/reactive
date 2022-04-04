package victor.training.reactive.reactor.lite.solved;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;
import victor.training.reactive.reactor.lite.Part08OtherOperations_easy;
import victor.training.reactive.reactor.lite.domain.User;

import java.util.List;

public class Part08OtherOperationsSolved extends Part08OtherOperations_easy {
   @Override
   public Flux<User> userFluxFromStringFlux(Flux<String> usernameFlux, Flux<String> firstnameFlux, Flux<String> lastnameFlux) {
      return Flux.zip(usernameFlux, firstnameFlux, lastnameFlux)
          .map(TupleUtils.function((username, firstName, lastName) ->
              new User(username, firstName, lastName)));
   }

   @Override
   public Mono<User> useFastestMono(Mono<User> mono1, Mono<User> mono2) {
      return Mono.firstWithValue(mono1, mono2);
   }

   @Override
   public Flux<User> useFastestFlux(Flux<User> flux1, Flux<User> flux2) {
      return Flux.firstWithValue(flux1, flux2);
   }

   @Override
   public Mono<Void> fluxCompletion(Flux<User> flux) {
      return flux.then();
   }

   @Override
   public Mono<User> nullAwareUserToMono(User user) {
      return Mono.justOrEmpty(user);
   }

   @Override
   public Mono<User> emptyToSkyler(Mono<User> mono) {
      return mono.defaultIfEmpty(User.SKYLER);
   }

   @Override
   public Mono<List<User>> fluxCollection(Flux<User> flux) {
      return flux.collectList();
   }
}
