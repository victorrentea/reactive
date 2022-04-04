package victor.training.reactive.usecase.grouping;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.PublisherProbe;
import reactor.test.publisher.TestPublisher;

import java.util.List;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class GroupingFluxesTest {
   @Mock
   Apis apis;
   @InjectMocks
   GroupingFluxes target;

   // solution:

   @Test
   void negative() {
      target.processMessageStream(Flux.just(-1)).block();

      Mockito.verifyNoMoreInteractions(apis);
   }

   @Test
   void odd() {
      PublisherProbe<Void> probeA = PublisherProbe.empty();
      when(apis.apiA(1)).thenReturn(probeA.mono());
      PublisherProbe<Void> probeB = PublisherProbe.empty();
      when(apis.apiB(1)).thenReturn(probeB.mono());

      target.processMessageStream(Flux.just(1)).block();

      assertThat(probeA.subscribeCount()).isEqualTo(1);
      assertThat(probeB.subscribeCount()).isEqualTo(1);
   }

   @Test
   void oddInParallel() {
      when(apis.apiA(1)).thenReturn(Mono.defer( // runs with virtual time
          () -> Mono.delay(ofSeconds(2)).then()));
      when(apis.apiB(1)).thenReturn(Mono.defer( // runs with virtual time
          () -> Mono.delay(ofSeconds(2)).then()));

      StepVerifier.withVirtualTime(() -> target.processMessageStream(Flux.just(1)))
          .expectSubscription()
          .thenAwait(ofSeconds(3))
          .expectComplete()
          .verify(ofSeconds(4));
   }
   
   @Test
   void even3() {
      PublisherProbe<Void> probe = PublisherProbe.empty();
      when(apis.apiC(List.of(2,4,6))).thenReturn(probe.mono());

      target.processMessageStream(Flux.just(2,4,6)).block();

      assertThat(probe.subscribeCount()).isEqualTo(1);
   }

   @Test
   void even2_bufferTimeout() {
      PublisherProbe<Void> probe = PublisherProbe.empty();
      when(apis.apiC(List.of(2,4))).thenReturn(probe.mono());

      TestPublisher<Integer> publisher = TestPublisher.<Integer>createCold()
          .next(2,4);

      StepVerifier.withVirtualTime(() -> target.processMessageStream(publisher.flux()))
          .thenAwait(ofMillis(450))
          .then(() -> assertThat(probe.subscribeCount()).isEqualTo(0))
          .thenAwait(ofMillis(100))
          .then(() -> assertThat(probe.subscribeCount()).isEqualTo(1))
          .then(() -> publisher.complete())
          .verifyComplete();
   }
}