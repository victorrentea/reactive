package victor.training.reactor.workshop;

import org.jooq.lambda.Unchecked;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;
import victor.training.reactor.workshop.P7_Flux.A;
import victor.training.reactor.workshop.P7_Flux.Dependency;
import victor.training.util.SubscribedProbe;
import victor.training.util.CaptureSystemOutputExtension;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static java.time.Duration.ofMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@TestMethodOrder(MethodName.class)
@ExtendWith(MockitoExtension.class)
public class P7_FluxTest {
  public static final List<Integer> LIST_OF_IDS = IntStream.range(0, 10).boxed().collect(toList());
  @Mock
  Dependency dependency;
  @InjectMocks
  protected P7_Flux workshop;

  @RegisterExtension
  SubscribedProbe subscribed = new SubscribedProbe();
  @RegisterExtension
  CaptureSystemOutputExtension systemOutput = new CaptureSystemOutputExtension();

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
  @Timeout(value = 500, unit = MILLISECONDS)
  void p01_fetchMany() {
    when(dependency.fetchOneById(any())).thenAnswer(call ->
            delayedMono(new A(call.getArgument(0)), 20, 100));

    List<A> results = workshop.p01_fetchMany(LIST_OF_IDS).collectList().block();

    assertThat(results).map(A::getId).describedAs("TODO1: Contains all elements").containsExactlyInAnyOrderElementsOf(LIST_OF_IDS);
    assertThat(systemOutput.toString()).describedAs("TODO2: Printed elements to console").contains("A(id=2)");
    assertThat(maxParallelism).describedAs("TODO3: Max number of requests in parallel").isLessThanOrEqualTo(4);
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
    when(dependency.fetchPageByIds(List.of(0, 1, 2, 3))).thenReturn(Flux.just(new A(0), new A(1), new A(2), new A(3)).delayElements(ofMillis(100)));
    when(dependency.fetchPageByIds(List.of(4, 5, 6, 7))).thenReturn(Flux.just(new A(4), new A(5), new A(6), new A(7)));
    when(dependency.fetchPageByIds(List.of(8, 9))).thenReturn(Flux.just(new A(8), new A(9)));

    List<A> results = workshop.p04_fetchInPages(Flux.fromIterable(LIST_OF_IDS)).collectList().block();

    assertThat(results).describedAs("All elements are present").map(A::getId).containsExactlyInAnyOrderElementsOf(LIST_OF_IDS);
    assertThat(results).describedAs("Elements are in the correct order (not scrambled)").map(A::getId).containsExactlyElementsOf(LIST_OF_IDS);
  }

  @Test
  @Timeout(value = 500, unit = MILLISECONDS)
  void p04_fetchInPages_delayedElement_KUNGFU_testPublisher_andStepVerifier() {
    when(dependency.fetchPageByIds(List.of(0))).thenReturn(Flux.just(new A(0)));

    TestPublisher<Integer> testPublisher = TestPublisher.create();
    workshop.p04_fetchInPages(testPublisher.flux())
            .as(StepVerifier::create)
            .then(() -> testPublisher.next(0))
            .thenAwait(ofMillis(300))
            .expectNext(new A(0))
            .then(() -> testPublisher.complete())
            .verifyComplete();
    // se poate si mai si, cu virtualTime (fakeuiest timpul sa nu stai efecti 400 ms la testul asta, ci doar cate ms)
  }


  @Nested
  class P05_InfiniteFlux {
    TestPublisher<Integer> publisher = TestPublisher.create();

    @BeforeEach
    final void before() {
      workshop.p05_infinite(publisher.flux());
    }

    @Test
    void negativesAreIgnored() {
      publisher.next(-1);
    }

    @Test
    void works() {
      when(dependency.fetchOneById(1)).thenReturn(subscribed.once(Mono.just(new A(1))));
      when(dependency.sendMessage(new A(1))).thenReturn(subscribed.once(Mono.empty()));
      publisher.next(1);
    }

    @Test
    void fetchFails_fluxNotCancelled() {
      when(dependency.fetchOneById(88)).thenReturn(Mono.error(new IllegalArgumentException("ExceptionFetch")));
      publisher.next(88);
      assertThat(systemOutput.toString()).contains("ExceptionFetch");
      publisher.assertWasNotCancelled();

      works(); // flux still works
    }

    @Test
    void anyError_fluxNotCancelled() {
      when(dependency.fetchOneById(99)).thenReturn(Mono.just(new A(99)));
      when(dependency.sendMessage(new A(99))).thenReturn(Mono.error(new IllegalArgumentException("ExceptionSend")));
      publisher.next(99);
      assertThat(systemOutput.toString()).contains("ExceptionSend");
      publisher.assertWasNotCancelled();

      works(); // flux still works
    }
  }

  
  @Test
  @Timeout(1)
  void p06_batchCalls() {
    workshop.p06_configureRequestFlux();
    when(dependency.fetchPageByIds(any())).thenAnswer(call -> Flux.fromIterable(
            ((List<Integer>) call.getArgument(0)).stream().map(A::new).collect(toList())
    ));

    List<Future<A>> futures = List.of(
            workshop.p06_submitRequest(1).toFuture(),
            workshop.p06_submitRequest(2).toFuture(),
            workshop.p06_submitRequest(3).toFuture(),
            workshop.p06_submitRequest(4).toFuture(),
            workshop.p06_submitRequest(5).toFuture()
    );
    List<A> results = futures.stream().map(Unchecked.function(Future::get)).collect(toList());
    assertThat(results).containsExactly(new A(1), new A(2), new A(3), new A(4), new A(5));
  }

  @Test
  @Timeout(value = 300, unit = MILLISECONDS)
  void p06_batchCalls_timeout() {
    workshop.p06_configureRequestFlux();
    when(dependency.fetchPageByIds(any())).thenAnswer(call -> Flux.fromIterable(
            ((List<Integer>) call.getArgument(0)).stream().map(A::new).collect(toList())
    ));

    assertThat(workshop.p06_submitRequest(1).block()).isEqualTo(new A(1));
  }
  @Test
  void p07_monitoring_detectsAllChanges() {
    List<Integer> results = workshop.p07_monitoring(Flux.just(1, -1, 2, -3, 0, -1)).collectList().block();

    assertThat(results).describedAs("#1 Detected all the changes").containsSubsequence(0, 1, 2, 3);
  }

  @Test
  void p07_monitoring_onlyEmitsNewValues() {
    List<Integer> results = workshop.p07_monitoring(Flux.just(1, -1, 2, -3, 0, -1)).collectList().block();

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
