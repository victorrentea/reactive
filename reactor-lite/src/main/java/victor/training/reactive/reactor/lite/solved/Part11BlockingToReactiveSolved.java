package victor.training.reactive.reactor.lite.solved;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import victor.training.reactive.reactor.lite.Part11BlockingToReactive;
import victor.training.reactive.reactor.lite.domain.User;
import victor.training.reactive.reactor.lite.repository.BlockingRepository;

import static reactor.core.scheduler.Schedulers.boundedElastic;

public class Part11BlockingToReactiveSolved extends Part11BlockingToReactive {

   @Override
   public Flux<User> blockingRepositoryToFlux(BlockingRepository<User> repository) {
      return Flux.defer(() -> Flux.fromIterable(repository.findAll()))
          .subscribeOn(boundedElastic());
   }

   @Override
   public Mono<Void> fluxToBlockingRepository(Flux<User> flux, BlockingRepository<User> repository) {
      return flux.flatMap(u -> Mono.fromRunnable(() -> repository.save(u)).subscribeOn(boundedElastic()))
          .then()
          ;
   }
}
