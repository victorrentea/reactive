package victor.training.reactive.reactor.lite.solved;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import victor.training.reactive.reactor.lite.Part06Request;
import victor.training.reactive.reactor.lite.domain.User;

public class Part06RequestSolved extends Part06Request {

   @Override
   public StepVerifier requestAllExpectFourThenComplete(Flux<User> flux) {
      return StepVerifier.create(flux)
          .expectNextCount(4)
          .expectComplete();
   }

   @Override
   public StepVerifier requestExpectThreeOrMore(Flux<User> flux) {
      return StepVerifier.create(flux)
          .expectNextCount(3)
          .thenConsumeWhile(u -> true)
          .expectComplete();
   }

   @Override
   public Flux<User> fluxWithLog() {
      return repository.findAll().log();
   }

   @Override
   public StepVerifier requestOne_oneMore_thenCancel(Flux<User> flux) {
      return StepVerifier.create(flux)
          .thenRequest(1)
          .expectNext(User.SKYLER)
          .thenRequest(1)
          .expectNext(User.JESSE)
          .thenCancel();
   }

   @Override
   public Flux<User> fluxWithDoOnPrintln() {
      return repository.findAll()
          .doOnSubscribe(s -> System.out.println("Starring:"))
          .doOnNext(u -> System.out.println(u.getFirstname() + " " + u.getLastname()))
          .doOnComplete(() -> System.out.println("The end!"));
   }

   @Override
   public Flux<Integer> throttleUpstreamRequest(Flux<Integer> upstream) {
      return upstream.limitRate(10);
   }
}
