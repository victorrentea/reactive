package victor.training.reactive.usecase.grouping;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.PublisherProbe;
import reactor.test.publisher.TestPublisher;

import java.util.List;

import static java.time.Duration.ofMillis;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
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
      // when
//      target.processMessageStream(Flux.just(-1)).subscribe(); // NEVER .subscribe in tests as it does NOT block the JUnit thread > the assertions start firing too early
      target.processMessageStream(Flux.just(-1)).block();

      // then
      Mockito.verifyNoMoreInteractions(apis);
   }

   @Test
   void apiA_and_B_areCalled_forOneOddNumber() {
      PublisherProbe<Void> apiAProbe = PublisherProbe.empty(); // counte # subscribers, cancel signal
      when(apis.apiA(1)).thenReturn(apiAProbe.mono());
      PublisherProbe<Void> apiBProbe = PublisherProbe.empty();
      when(apis.apiB(1)).thenReturn(apiBProbe.mono());

      target.processMessageStream(Flux.just(1)).block();

      assertThat(apiAProbe.subscribeCount()).isEqualTo(1); // EXACTLY ONE NETWORK CALL HAPPENED
      assertThat(apiBProbe.subscribeCount()).isEqualTo(1);
   }
   @Test
   void apiA_and_B_areCalled_for2OddNumbers() {
      PublisherProbe<Void> apiAProbe = PublisherProbe.empty();
      when(apis.apiA(anyInt())).thenReturn(apiAProbe.mono());
      PublisherProbe<Void> apiBProbe = PublisherProbe.empty();
      when(apis.apiB(anyInt())).thenReturn(apiBProbe.mono());

      target.processMessageStream(Flux.just(1, 3)).block();

      assertThat(apiAProbe.subscribeCount()).isEqualTo(2);
      assertThat(apiBProbe.subscribeCount()).isEqualTo(2);
      verify(apis).apiA(1);
      verify(apis).apiA(3);
      verify(apis).apiB(1);
      verify(apis).apiB(3);
   }

   @Test
   void apiA_and_B_areCalled_inParallel() {
      when(apis.apiA(1)).thenReturn(Mono.delay(ofMillis(500)).then());
      when(apis.apiB(1)).thenReturn(Mono.delay(ofMillis(500)).then());

      target.processMessageStream(Flux.just(1)).block(ofMillis(600));
   }

   @Test
   void apiA_and_B_areCalled_inParallel_withVirtualTime() {
      when(apis.apiA(1)).thenReturn( Mono.defer(()->   Mono.delay(ofMillis(5000)).then()   ));
      when(apis.apiB(1)).thenReturn( Mono.defer(()-> Mono.delay(ofMillis(5000)).then()));



//      StepVerifier.create(  target.processMessageStream(Flux.just(1))  )
      StepVerifier.withVirtualTime( ()->  target.processMessageStream(Flux.just(1))  )
//          .thenRequest(4) // fires a request upstream to the tested Flux to emit N elements
//          .expectNextCount(4)
          .thenAwait(ofMillis(6000))
          .expectComplete()
          .verify(ofMillis(10));
   }

   @Test
   void apiCisCalled_withAListOf3_after3EvenNumbers() {
   }

   @Test
   void apiC_isCalled_withAListOf2_after2EvenNumbers_and500MillisElapsed() {
      PublisherProbe<Void> probe = PublisherProbe.empty();
      when(apis.apiC(List.of(2,4))).thenReturn(probe.mono());

      TestPublisher<Integer> publisher = TestPublisher.<Integer>createCold()
          .next(2,4)
          ;

//      Flux.defer(() -> Flux.interval(ofMillis(100)))

      StepVerifier.withVirtualTime(() -> target.processMessageStream(publisher.flux()))
          .thenAwait(ofMillis(450))
          .then(() -> probe.assertWasNotSubscribed())
          .thenAwait(ofMillis(100))
          .then(() ->  probe.assertWasSubscribed())
          .then(() -> publisher.complete())
          .verifyComplete();
   }
}