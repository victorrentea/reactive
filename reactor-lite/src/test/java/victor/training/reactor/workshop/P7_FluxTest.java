package victor.training.reactor.workshop;

import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;
import victor.training.reactor.workshop.P7_Flux.A;
import victor.training.reactor.workshop.P7_Flux.Dependency;
import victor.training.util.CaptureSystemOutput;
import victor.training.util.CaptureSystemOutput.OutputCapture;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.LongStream;

import static java.time.Duration.ofMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static victor.training.reactor.lite.Utils.sleep;

@TestMethodOrder(MethodName.class)
@ExtendWith(MockitoExtension.class)
public class P7_FluxTest {
  public static final List<Long> LIST_OF_IDS = LongStream.range(0, 10).boxed().collect(toList());
  @Mock
  Dependency dependency;
  @InjectMocks
  P7_Flux workshop;
  //  P7_FluxSolved workshop;

  AtomicInteger parallelCallsCounter = new AtomicInteger();
  int maxParallelism = 0;
  Random random = new Random();

  private Mono<A> delayedMono(A value, int minDelayMillis, int maxDelayMillis) {
    return Mono.just(value)
            .delayElement(ofMillis((long) (minDelayMillis + (maxDelayMillis - minDelayMillis) * random.nextDouble())))
            .doOnSubscribe(s -> {
              int parallelCalls = parallelCallsCounter.incrementAndGet();
              // System.out.println("Call (concurrent #" + parallelCalls + ") for " + value);
              maxParallelism = Math.max(maxParallelism, parallelCalls);
            })
            .doOnNext(e -> parallelCallsCounter.decrementAndGet());
  }

  @Test
  @CaptureSystemOutput
  @Timeout(value = 500, unit = MILLISECONDS)
  void p01_fetchInParallel_scrambled(OutputCapture capture) {
    when(dependency.fetchOneById(any())).thenAnswer(call ->
            delayedMono(new A(call.getArgument(0)), 20, 100));

    List<A> results = workshop.p01_fetchInParallel_scrambled(LIST_OF_IDS).collectList().block();

    assertThat(results).map(A::getId).describedAs("Contains all elements").containsExactlyInAnyOrderElementsOf(LIST_OF_IDS);
    assertThat(capture.toString()).describedAs("Printed elements to console").contains("A(id=2)");
    assertThat(maxParallelism).describedAs("Max number of requests in parallel").isLessThanOrEqualTo(4);

  }


  @Test
  @Timeout(value = 300, unit = MILLISECONDS)
  void p02_fetchInParallel_preservingOrder() {
    when(dependency.fetchOneById(any())).thenAnswer(call ->
            delayedMono(new A(call.getArgument(0)), 50, 100));

    List<A> results = workshop.p02_fetchInParallel_preservingOrder(LIST_OF_IDS).collectList().block();

    assertThat(results).map(A::getId).containsExactlyElementsOf(LIST_OF_IDS);
  }

  @Test
  void p03_fetchOneByOne() {
    when(dependency.fetchOneById(any())).thenAnswer(call ->
            delayedMono(new A(call.getArgument(0)), 50, 100));

    List<A> results = workshop.p03_fetchOneByOne(LIST_OF_IDS).collectList().block();

    assertThat(results).map(A::getId).containsExactlyElementsOf(LIST_OF_IDS);
    assertThat(maxParallelism).isEqualTo(1);
  }


  @Test
  void p04_fetchInPages() {
    when(dependency.fetchPageByIds(List.of(0L, 1L, 2L, 3L))).thenReturn(Flux.just(new A(0L), new A(1L), new A(2L), new A(3L)).delayElements(ofMillis(100)));
    when(dependency.fetchPageByIds(List.of(4L, 5L, 6L, 7L))).thenReturn(Flux.just(new A(4L), new A(5L), new A(6L), new A(7L)));
    when(dependency.fetchPageByIds(List.of(8L, 9L))).thenReturn(Flux.just(new A(8L), new A(9L)));

    List<A> results = workshop.p04_fetchInPages(Flux.fromIterable(LIST_OF_IDS)).collectList().block();

    assertThat(results).describedAs("All elements are present").map(A::getId).containsExactlyInAnyOrderElementsOf(LIST_OF_IDS);
    assertThat(results).describedAs("Elements are in the correct order (not scrambled)").map(A::getId).containsExactlyElementsOf(LIST_OF_IDS);
  }

  @Test
  @Timeout(value = 500, unit = MILLISECONDS)
  void p04_fetchInPages_delayedElement() {
    when(dependency.fetchPageByIds(List.of(0L))).thenReturn(Flux.just(new A(0L)));

    Flux<Long> emit0_thenCompleteAfter300ms = Flux.just(0L).concatWith(Flux.interval(ofMillis(300)).take(1).filter(x -> false));
    List<A> results = workshop.p04_fetchInPages(emit0_thenCompleteAfter300ms).collectList().block();

    assertThat(results.get(0)).isEqualTo(new A(0L));
  }

  @Test
  @Timeout(value = 500, unit = MILLISECONDS)
  void p04_fetchInPages_delayedElement_testPublisherParallel() {
    when(dependency.fetchPageByIds(List.of(0L))).thenReturn(Flux.just(new A(0L)));

    TestPublisher<Long> testPublisher = TestPublisher.create();
    CompletableFuture.runAsync(() ->
            {
              sleep(100);// posibil sa faca testul flaky
              testPublisher.next(0L);
              sleep(300);
              testPublisher.complete();
            }

    );
    List<A> results = workshop.p04_fetchInPages(testPublisher.flux()).collectList().block();
    assertThat(results.get(0)).isEqualTo(new A(0L));
  }

  @Test
  @Timeout(value = 500, unit = MILLISECONDS)
  void p04_fetchInPages_delayedElement_testPublisher_andStepVerifier() {
    when(dependency.fetchPageByIds(List.of(0L))).thenReturn(Flux.just(new A(0L)));

    TestPublisher<Long> testPublisher = TestPublisher.create();
    workshop.p04_fetchInPages(testPublisher.flux())
            .as(StepVerifier::create)
            .then(() -> testPublisher.next(0L))
            .thenAwait(ofMillis(300))
            .expectNext(new A(0L))
            .then(() -> testPublisher.complete())
            .verifyComplete();
    // se poate si mai si, cu virtualTime (fakeuiest timpul sa nu stai efecti 400 ms la testul asta, ci doar cate ms)
  }


  @Test
  @CaptureSystemOutput
  void p05_infinite(OutputCapture outputCapture) {
    TestPublisher<Long> publisher = TestPublisher.create();
    when(dependency.fetchOneById(any())).thenAnswer(p -> Mono.just(new A(p.getArgument(0))));
    when(dependency.sendMessage(any())).thenReturn(Mono.empty());

    workshop.p05_infinite(publisher.flux());

    // ignored
    publisher.next(-1L);
    sleep(100);
    verify(dependency, never()).sendMessage(any());

    // passes through
    publisher.next(1L);
    sleep(100);
    verify(dependency).fetchOneById(1L);
    verify(dependency).sendMessage(new A(1L));

    // fetch fails
    when(dependency.fetchOneById(88L)).thenReturn(Mono.error(new IllegalArgumentException("ExceptionFetch")));
    publisher.next(88L);
    sleep(100);
    assertThat(outputCapture.toString()).contains("ExceptionFetch");
    publisher.assertWasNotCancelled();

    // send fails
    when(dependency.sendMessage(new A(99L))).thenReturn(Mono.error(new IllegalArgumentException("ExceptionSend")));
    publisher.next(99L);
    sleep(100);
    assertThat(outputCapture.toString()).contains("ExceptionSend");
    publisher.assertWasNotCancelled();

    // flux still works
    publisher.next(2L);
    sleep(100);
    verify(dependency).fetchOneById(2L);
    verify(dependency).sendMessage(new A(2L));
  }

  @Test
  void p06_monitoring() {

    List<Integer> results = workshop.p06_monitoring(Flux.just(1, -1, 2, -3, 0, -1)).collectList().block();

    assertThat(results).describedAs("#1 Detected all the changes").containsSubsequence(0, 1, 2, 3);

    assertThat(results).describedAs("#2 Does not emit duplicates").containsExactly(0, 1, 2, 3);
  }

  @Test
  void p09_groupedFlux_0_basic() {
    when(dependency.sendOdd1(any())).thenReturn(Mono.empty());
    when(dependency.sendOdd2(any())).thenReturn(Mono.empty());
    when(dependency.sendEven(any())).thenReturn(Mono.empty());

    workshop.p09_groupedFlux(Flux.just(2, 1, -1)).block();

    verify(dependency).sendOdd1(1);
    verify(dependency).sendOdd2(1);
    verify(dependency).sendEven(List.of(2));
  }

  @Test
  void p09_groupedFlux_1_pagingEven() {
    when(dependency.sendOdd1(any())).thenReturn(Mono.empty());
    when(dependency.sendOdd2(any())).thenReturn(Mono.empty());
    when(dependency.sendEven(any())).thenReturn(Mono.empty());

    workshop.p09_groupedFlux(Flux.just(2, 4, -1, 6, 4, 5)).block();

    verify(dependency).sendEven(List.of(2, 4, 6));
    verify(dependency).sendEven(List.of(4));
  }

}
