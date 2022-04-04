package victor.training.reactive.reactor.lite.solved;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import victor.training.reactive.reactor.lite.Part09Adapt;
import victor.training.reactive.reactor.lite.domain.User;

import java.util.concurrent.CompletableFuture;

public class Part09AdaptSolved extends Part09Adapt {
   @Override
   public Flowable<User> fromFluxToFlowable(Flux<User> flux) {
      return Flowable.fromPublisher(flux);
   }

   @Override
   public Flux<User> fromFlowableToFlux(Flowable<User> flowable) {
      return Flux.from(flowable);
   }

   @Override
   public Observable<User> fromFluxToObservable(Flux<User> flux) {
      return Observable.fromPublisher(flux);
   }

   @Override
   public Flux<User> fromObservableToFlux(Observable<User> observable) {
      return Flux.from(Flowable.fromObservable(observable, BackpressureStrategy.BUFFER));
   }

   @Override
   public Single<User> fromMonoToSingle(Mono<User> mono) {
      return Single.fromPublisher(mono);
   }

   @Override
   public Mono<User> fromSingleToMono(Single<User> single) {
      return Mono.from(Flowable.fromSingle(single));
   }

   @Override
   public CompletableFuture<User> fromMonoToCompletableFuture(Mono<User> mono) {
      return mono.toFuture();
   }

   @Override
   public Mono<User> fromCompletableFutureToMono(CompletableFuture<User> future) {
      return Mono.fromCompletionStage(future);
   }
}
