package victor.training.reactor.workshop;

import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import victor.training.reactor.lite.Utils;
import victor.training.reactor.workshop.P7_Flux.A;
import victor.training.reactor.workshop.P7_Flux.Dependency;
import victor.training.util.CaptureSystemOutput;
import victor.training.util.CaptureSystemOutput.OutputCapture;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.LongStream;

import static java.time.Duration.ofMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodName.class)
@ExtendWith(MockitoExtension.class)
public class P7_FluxTest {
  public static final List<Long> LIST_OF_IDS = LongStream.range(0, 10).boxed().collect(toList());
  @Mock
  Dependency dependency;
  @InjectMocks
  P7_Flux workshop;

  AtomicInteger parallelCallsCounter = new AtomicInteger();
  int maxParallelism = 0;
  Random random = new Random();

  private Mono<A> delayedMono(A value, int minDelayMillis, int maxDelayMillis) {
    return Mono.just(value)
            .delayElement(ofMillis((long) (minDelayMillis + maxDelayMillis * random.nextDouble())))
            .doOnSubscribe(s -> {
              int parallelCalls = parallelCallsCounter.incrementAndGet();
              System.out.println("Call (concurrent #" + parallelCalls + ") for " + value);
              maxParallelism = Math.max(maxParallelism, parallelCalls);
            })
            .doOnNext(e -> parallelCallsCounter.decrementAndGet());
  }

  @Test
  @CaptureSystemOutput
  void p01_fetchInParallel_scrambled(OutputCapture capture) {
    when(dependency.fetchOneById(any())).thenAnswer(call ->
            delayedMono(new A((Long) call.getArguments()[0]), 20, 100));

    List<A> results = workshop.p01_fetchInParallel_scrambled(LIST_OF_IDS).collectList().block();

    assertThat(results).map(A::getId).containsExactlyInAnyOrderElementsOf(LIST_OF_IDS);
    assertThat(capture.toString()).contains("A(id=2)");
  }

  @Test
  void p01_fetchInParallel_scrambled_max4Concurrent() {
    when(dependency.fetchOneById(any())).thenAnswer(call ->
            delayedMono(new A((Long) call.getArguments()[0]), 20, 100));

    workshop.p01_fetchInParallel_scrambled(LIST_OF_IDS).blockLast();

    assertThat(maxParallelism).isLessThanOrEqualTo(4);
  }

  @Test
  @Timeout(value = 200, unit = MILLISECONDS)
  void p02_fetchInParallel_preservingOrder() {
    when(dependency.fetchOneById(any())).thenAnswer(call ->
            delayedMono(new A((Long) call.getArguments()[0]), 50, 100));

    List<A> results = workshop.p02_fetchInParallel_preservingOrder(LIST_OF_IDS).collectList().block();

    assertThat(results).map(A::getId).containsExactlyElementsOf(LIST_OF_IDS);
  }

  @Test
  void p03_fetchOneByOne() {
    when(dependency.fetchOneById(any())).thenAnswer(call ->
            delayedMono(new A((Long) call.getArguments()[0]), 50, 100));

    List<A> results = workshop.p03_fetchOneByOne(LIST_OF_IDS).collectList().block();

    assertThat(results).map(A::getId).containsExactlyElementsOf(LIST_OF_IDS);
    assertThat(maxParallelism).isEqualTo(1);
  }


  @Test
  void p04_fetchInPages() {
    when(dependency.fetchPageByIds(List.of(0L, 1L, 2L, 3L))).thenReturn(Flux.just(new A(0L), new A(1L), new A(2L), new A(3L)));
    when(dependency.fetchPageByIds(List.of(4L, 5L, 6L, 7L))).thenReturn(Flux.just(new A(4L), new A(5L), new A(6L), new A(7L)));
    when(dependency.fetchPageByIds(List.of(8L, 9L))).thenReturn(Flux.just(new A(8L), new A(9L)));
    when(dependency.sendMessage(any())).thenReturn(Mono.empty());

    workshop.p04_fetchInPages(Flux.fromIterable(LIST_OF_IDS));

    Utils.sleep(100);

    ArgumentCaptor<A> aCaptor = ArgumentCaptor.forClass(A.class);
    verify(dependency,times(10)).sendMessage(aCaptor.capture());
    assertThat(aCaptor.getAllValues()).map(A::getId).containsExactlyElementsOf(LIST_OF_IDS);
  }


}
