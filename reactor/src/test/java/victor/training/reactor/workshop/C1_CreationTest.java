package victor.training.reactor.workshop;

import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import victor.training.util.CaptureSystemOutputExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.*;

@TestMethodOrder(MethodName.class)
public class C1_CreationTest {

  protected C1_Creation workshop = new C1_Creation();

  @RegisterExtension
  CaptureSystemOutputExtension systemOutput = new CaptureSystemOutputExtension();

  @Test
  void p1_mono1_just() {
    Mono<String> mono = workshop.mono1_just();
    assertThat(mono.block()).isEqualTo("foo");
  }

  @Test
  void p1_mono2_empty() {
    workshop.mono2_empty()
            .as(StepVerifier::create)
            .verifyComplete();
  }

  @Test
  void p1_mono3_optional() {
    assertThat(workshop.mono3_optional("foo").block()).isEqualTo("foo");

    workshop.mono3_optional(null)
            .as(StepVerifier::create)
            .verifyComplete();
  }

  @Test
  void p1_mono4_error() {
    workshop.mono4_error()
            .as(StepVerifier::create)
            .verifyError(IllegalStateException.class);
  }

  @Test
  void p1_mono5_noSignal() {
    workshop.mono5_noSignal()
            .as(StepVerifier::create)
            .expectSubscription()
            .expectTimeout(ofSeconds(1))
            .verify();
  }


  @Test
  @Timeout(value = 200, unit = MILLISECONDS)
  void p1_mono6_delayedData() {
    workshop.mono6_delayedData()
            .as(StepVerifier::create)
            .expectSubscription()
            .expectNoEvent(ofMillis(50))
            .expectNext("BOO")
            .verifyComplete();
  }

  @Test
  void p1_mono7_fromCallable() throws InterruptedException {
    Mono<LocalDateTime> resultMono = workshop.mono7_fromCallable();
    Thread.sleep(110);
    LocalDateTime time = resultMono.block();
    assertThat(time).isCloseTo(LocalDateTime.now(), byLessThan(100, ChronoUnit.MILLIS));
  }


  @Test
  @Timeout(value = 300, unit = MILLISECONDS)
  void p1_mono8_delayedCompletion() throws InterruptedException {
    Mono<Void> mono = workshop.mono8_delayedCompletion();
    Thread.sleep(110);
    mono
            .as(StepVerifier::create)
            .expectSubscription()
            .expectNoEvent(ofMillis(50))
            .verifyComplete();
  }


  @Test
  void p2_flux1_values() {
    workshop.flux1_values()
            .as(StepVerifier::create)
            .expectNext("foo", "bar")
            .verifyComplete();
  }

  @Test
  void p2_flux2_fromList() {
    workshop.flux2_fromList(List.of("foo", "bar"))
            .as(StepVerifier::create)
            .expectNext("foo", "bar")
            .verifyComplete();
  }

  @Test
  void p2_flux3_empty() {
    workshop.flux3_empty()
            .as(StepVerifier::create)
            .verifyComplete();
  }

  @Test
  void p2_flux4_error() {
    workshop.flux4_error()
            .as(StepVerifier::create)
            .verifyError(IllegalStateException.class);
  }

  @Test
  @Timeout(value = 1500, unit = MILLISECONDS)
  void p2_flux5_delayedElements() {
    Duration duration = workshop.flux5_delayedElements()
            .as(StepVerifier::create)
//            .expectNext(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L)
        // backwards:
            .expectNext(9L, 8L, 7L, 6L, 5L, 4L, 3L, 2L, 1L, 0L)

            .verifyComplete();
    assertThat(duration.toMillis())
            .describedAs("Should take approx 1 second")
            .isGreaterThan(900).isLessThan(1200);
  }


  @Test
  void p3_logSignals() {
    Flux<String> flux = Flux.just("one", "two");
    workshop.logSignals(flux).collectList().block();
    assertThat(systemOutput.toString())
            .contains("onSubscribe", "request", "onNext", "onComplete");
  }

  @Test
  void p3_doOnHooks() {
    Flux<String> flux = Flux.just("one", "two");

    workshop.doOnHooks(flux).blockLast();

    assertThat(systemOutput.toString())
            .contains("SUBSCRIBE", "NEXT", "one", "two", "COMPLETE", "END");
  }

  @Test
  void p3_doOnHooks_error() {
    Flux<String> flux = Flux.error(new IllegalStateException());

    assertThatThrownBy(() -> workshop.doOnHooks(flux).blockLast());

    assertThat(systemOutput.toString())
            .contains("SUBSCRIBE", "ERROR", "END");
  }

  @Test
  void reactorContext_read() {
    String string = workshop.reactorContext_read()
            .contextWrite(context -> context.put("username", "Joe"))
            .block();

    assertThat(string).contains("Hi", "Joe");
  }
}
