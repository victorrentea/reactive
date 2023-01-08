package victor.training.reactor.workshop;

import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import victor.training.reactor.workshop.P3_Errors.Dependency;
import victor.training.util.CaptureSystemOutputExtension;
import victor.training.util.SubscribedProbe;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static reactor.core.publisher.Mono.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodName.class)
@Timeout(1)
public class P3_ErrorsTest {
  @Mock
  Dependency dependencyMock;
  @InjectMocks
  protected P3_Errors workshop;

  @RegisterExtension
  SubscribedProbe subscribed = new SubscribedProbe();
  @RegisterExtension
  CaptureSystemOutputExtension systemOutput = new CaptureSystemOutputExtension();

  public static class TestRootCauseException extends RuntimeException {
  }


  @Test
  void p01_log_KO() {
    when(dependencyMock.call()).thenReturn(Mono.error(new TestRootCauseException()));

    assertThatThrownBy(() -> workshop.p01_log_rethrow().block())
            .isInstanceOf(TestRootCauseException.class);

    assertThat(systemOutput.toString())
            .contains(TestRootCauseException.class.getSimpleName());
  }

  @Test
  void p01_log_OK() {
    when(dependencyMock.call()).thenReturn(just("abc"));

    workshop.p01_log_rethrow().block();

    assertThat(systemOutput.toString()).isEmpty();
  }

  @Test
  void p02_wrap() {
    when(dependencyMock.call()).thenReturn(Mono.error(new TestRootCauseException()));

    Mono<String> result = workshop.p02_wrap();

    assertThatThrownBy(result::block)
            .isInstanceOf(IllegalStateException.class) // get() wraps the exception within an ExecutionException
            .hasRootCauseInstanceOf(TestRootCauseException.class) // added original exception as the cause
    ;
  }

  @Test
  void p03_defaultValue_KO() {
    when(dependencyMock.call()).thenReturn(Mono.error(new TestRootCauseException()));
    assertThat(workshop.p03_defaultValue().block()).isEqualTo("default");
  }

  @Test
  void p03_defaultValue_OK() {
    when(dependencyMock.call()).thenReturn(just("OK"));
    assertThat(workshop.p03_defaultValue().block()).isEqualTo("OK");
  }


  @Test
  void p04_defaultFuture_OK() {
    when(dependencyMock.call()).thenReturn(just("OK"));
    assertThat(workshop.p04_fallback().block()).isEqualTo("OK");
  }

  @Test
  void p04_defaultFuture_KO() {
    when(dependencyMock.call()).thenReturn(error(new TestRootCauseException()));
    when(dependencyMock.backup()).thenReturn(just("backup"));
    assertThat(workshop.p04_fallback().block()).isEqualTo("backup");
  }

  @Test
  void p05_sendError_OK() {
    when(dependencyMock.call()).thenReturn(just("ok"));
    assertThat(workshop.p05_sendError().block()).isEqualTo("ok");
    verify(dependencyMock, Mockito.never()).sendError(any());
  }

  @Test
  void p05_sendError_KO() {
    TestRootCauseException ex = new TestRootCauseException();
    when(dependencyMock.call()).thenReturn(error(ex));
    when(dependencyMock.sendError(ex)).thenReturn(subscribed.once(empty()));

    assertThatThrownBy(() -> workshop.p05_sendError().block())
            .isInstanceOf(TestRootCauseException.class);
  }

  @Test
  void p06_retryThenLogError_failingAlways() {
    TestRootCauseException ex = new TestRootCauseException();
    when(dependencyMock.call()).thenReturn(subscribed.times(4, error(ex)));

    assertThatThrownBy(() -> workshop.p06_retryThenLogError().block())
            .isInstanceOf(TestRootCauseException.class);
    assertThat(systemOutput.toString()).contains("SCRAP LOGS FOR ME");
  }

  @Test
  void p06_retryThenLogError_failing2x() {
    AtomicInteger iteration = new AtomicInteger(0);
    when(dependencyMock.call()).thenReturn(subscribed.times(3, failingMonoTimes(2, "result")));

    assertThat(workshop.p06_retryThenLogError().block()).isEqualTo("result");
    assertThat(systemOutput.toString()).doesNotContain("SCRAP LOGS FOR ME");
  }

  @Test
  void p06_retryThenLogError_failing3x() {
    AtomicInteger iteration = new AtomicInteger(0);
    when(dependencyMock.call()).thenReturn(subscribed.times(4, failingMonoTimes(3, "result")));

    assertThat(workshop.p06_retryThenLogError().block()).isEqualTo("result");
    assertThat(systemOutput.toString()).doesNotContain("SCRAP LOGS FOR ME");
  }

  private static Mono<String> failingMonoTimes(int timesFailing, String result) {
    AtomicInteger iteration = new AtomicInteger(0);
    return create(sink -> {
      if (iteration.incrementAndGet() <= timesFailing) {
        sink.error(new TestRootCauseException());
      } else {
        sink.success(result);
      }
    });
  }


  @Test
  void p06_retryThenLogError_failing0x() {
    when(dependencyMock.call())
            .thenReturn(subscribed.once(just("result")))
    ;

    assertThat(workshop.p06_retryThenLogError().block()).isEqualTo("result");
    assertThat(systemOutput.toString()).doesNotContain("SCRAP LOGS FOR ME");
  }


  @Test
  void p06_retryThenLogError_timeout() {
    when(dependencyMock.call()).thenReturn(subscribed.times(4, never()));

    assertThatThrownBy(() -> workshop.p06_retryThenLogError().block());
    assertThat(systemOutput.toString()).contains("SCRAP LOGS FOR ME");
  }


  @Test
  @Timeout(5)
  void p07_retryWithBackoff_failingAlways() {
    TestRootCauseException ex = new TestRootCauseException();
    when(dependencyMock.call()).thenReturn(subscribed.times(4, error(ex)));

    Duration delta = workshop.p07_retryWithBackoff()
            .as(StepVerifier::create)
            .expectError().verify();

    assertThat(delta.toMillis()).isCloseTo(200 + 400 + 800, byLessThan(750L));
  }

  @Test
  @Timeout(5)
  void p07_retryWithBackoff_failing2x() {
    when(dependencyMock.call()).thenReturn(subscribed.times(3, failingMonoTimes(2, "result")));

    Duration delta = workshop.p07_retryWithBackoff()
            .as(StepVerifier::create)
            .expectNext("result")
            .verifyComplete();

    assertThat(delta.toMillis()).isCloseTo(200 + 400, byLessThan(350L));
  }

  @Test
  void p07_retryWithBackoff_failing0x() {
    when(dependencyMock.call()).thenReturn(subscribed.times(1, just("result")));

    Duration delta = workshop.p07_retryWithBackoff()
            .as(StepVerifier::create)
            .expectNext("result")
            .verifyComplete();

    assertThat(delta.toMillis()).isCloseTo(0L, byLessThan(90L));
  }

  @Test
  void p08_usingResourceThatNeedsToBeClosed() throws IOException {
    when(dependencyMock.downloadManyElements()).thenReturn(Flux.just("abc", "def"));

    workshop.p08_usingResourceThatNeedsToBeClosed().block();

    File file = new File("out.txt");
    assertThat(Files.readString(file.toPath())).isEqualTo("abcdef");
    assertThat(file.delete()).isTrue();
  }


}