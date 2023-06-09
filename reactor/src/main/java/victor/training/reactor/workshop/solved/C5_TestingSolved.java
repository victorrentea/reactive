package victor.training.reactor.workshop.solved;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;
import victor.training.reactor.lite.domain.User;
import victor.training.reactor.workshop.C5_Testing;

import java.time.Duration;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class C5_TestingSolved extends C5_Testing {
   @Override
   public void expectFooBarComplete(Flux<String> flux) {
      StepVerifier.create(flux)
          .expectNext("foo", "bar")
          .verifyComplete();
   }

   @Override
   public void expectFooBarError(Flux<String> flux) {
      StepVerifier.create(flux)
          .expectNext("foo","bar")
          .verifyError(RuntimeException.class);
   }

   @Override
   public void expectSkylerJesseComplete(Flux<User> flux) {
      StepVerifier.create(flux)
          .expectNextMatches(u -> "swhite".equals(u.getUsername()))
          .expectNextMatches(u -> "jpinkman".equals(u.getUsername()))
          .verifyComplete();
   }


   @Override
   public void expectDelayedElement() {
      StepVerifier.withVirtualTime(() -> timeBoundFlow())
          .expectSubscription()
          .expectNoEvent(Duration.ofMinutes(30))
          .thenAwait(Duration.ofHours(1))
          .expectNextCount(1)
          .verifyComplete();
   }

   @Override
   public void verifySubscribedOnce(Function<TestedProdClass, Mono<Void>> testedRxCode) {
      // given
      SomeRxRepo mockRepo = mock(SomeRxRepo.class);
      TestedProdClass testedObject = new TestedProdClass(mockRepo);

      TestPublisher<Void> saveMono = TestPublisher.createCold();
      when(mockRepo.save(User.SKYLER)).thenReturn(saveMono.mono());
      saveMono.complete(); // already completed

      // when
      testedRxCode.apply(testedObject).block();

      // then
      assertThat(saveMono.subscribeCount())
          .describedAs("0 means no network call, >1 means repeated network calls")
          .isEqualTo(1);
   }

}
