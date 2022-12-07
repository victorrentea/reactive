package victor.training.reactor.workshop;

import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Timeout;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import victor.training.util.CaptureSystemOutput;
import victor.training.util.CaptureSystemOutput.OutputCapture;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;

@TestMethodOrder(MethodName.class)
public class P1_CreationTest {

  	P1_Creation workshop = new P1_Creation();
//  P1_Creation workshop = new P1_CreationSolved();


  @Test
  public void mono1_just() {
    Mono<String> mono = workshop.mono1_just();
    assertThat(mono.block()).isEqualTo("foo");
  }

  @Test
  public void mono2_empty() {
    workshop.mono2_empty()
            .as(StepVerifier::create)
            .verifyComplete();
  }

  @Test
  public void mono3_optional() {
    assertThat(workshop.mono3_optional("foo").block()).isEqualTo("foo");

    workshop.mono3_optional(null)
            .as(StepVerifier::create)
            .verifyComplete();
  }

  @Test
  public void mono4_error() {
    workshop.mono4_error()
            .as(StepVerifier::create)
            .verifyError(IllegalStateException.class);
  }

  @Test
  public void mono5_noSignal() {
    workshop.mono5_noSignal()
            .as(StepVerifier::create)
            .expectSubscription()
            .expectTimeout(ofSeconds(1))
            .verify();
  }



  @Test
  @Timeout(value = 200, unit = MILLISECONDS)
  void mono6_delayedData() {
    workshop.mono6_delayedData()
            .as(StepVerifier::create)
            .expectSubscription()
            .expectNoEvent(ofMillis(50))
            .expectNext("BOO")
            .verifyComplete();
  }

  @Test
  void mono7_fromCallable() throws InterruptedException {
    Mono<LocalDateTime> resultMono = workshop.mono7_fromCallable();
    Thread.sleep(110);
    LocalDateTime time = resultMono.block();
    assertThat(time).isCloseTo(LocalDateTime.now(), byLessThan(100, ChronoUnit.MILLIS));
  }


  @Test
  @Timeout(value = 200, unit = MILLISECONDS)
  void mono8_delayedCompletion() {
    workshop.mono8_delayedCompletion()
            .as(StepVerifier::create)
            .expectSubscription()
            .expectNoEvent(ofMillis(50))
            .verifyComplete();
  }


  @Test
  public void flux1_values() {
    workshop.flux1_values()
            .as(StepVerifier::create)
            .expectNext("foo", "bar")
            .verifyComplete();
  }

  @Test
  public void flux2_fromList() {
    workshop.flux2_fromList(List.of("foo", "bar"))
            .as(StepVerifier::create)
            .expectNext("foo", "bar")
            .verifyComplete();
  }
  @Test
  public void flux3_empty() {
    workshop.flux3_empty()
            .as(StepVerifier::create)
            .verifyComplete();
  }

  @Test
  public void flux4_error() {
    workshop.flux4_error()
            .as(StepVerifier::create)
            .verifyError(IllegalStateException.class);
  }

  @Test
  @Timeout(value = 1500, unit = MILLISECONDS)
  public void flux5_delayedElements() {
    Duration duration = workshop.flux5_delayedElements()
            .as(StepVerifier::create)
            .expectNext(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L)
            .verifyComplete();
    assertThat(duration.toMillis())
            .describedAs("Should take approx 1 second")
            .isGreaterThan(900).isLessThan(1200);
  }


  @Test
  @CaptureSystemOutput
  void logSignals(OutputCapture outputCapture) {
    Flux<String> flux = Flux.just("one", "two");
    workshop.logSignals(flux).collectList().block();
    assertThat(outputCapture.toString())
            .contains("onSubscribe", "request", "onNext", "onComplete");
  }

}
