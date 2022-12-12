package victor.training.reactor.workshop;

import lombok.ToString;
import lombok.Value;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class P7_Flux {
  public P7_Flux(Dependency dependency) {
    this.dependency = dependency;
  }
  private final Dependency dependency;

  @Value
  static class A{Long id;}
  interface Dependency {
    Mono<A> fetchOneById(Long id);
    Flux<A> fetchPageByIds(List<Long> idPage);
    Mono<Void> sendMessage(A a);
  }

  // ==================================================================================
  // TODO fetch elements by id as fast as possible
  // TODO #2 Print elements as they come in. What do you observe?
  // TODO #3 Restrict the concurrency to maximum 4 requests in parallel
  // Hint: start with Flux.fromIterable(...)
  // Bonus: Write code to count how many calls happen in parallel
  public Flux<A> p01_fetchInParallel_scrambled(List<Long> idList) {
    return Flux.fromIterable(idList)
            .flatMap(dependency::fetchOneById, 4)
            .doOnNext(e -> System.out.println("Element: " + e))
            ;
  }

  public Flux<A> p02_fetchInParallel_preservingOrder(List<Long> idList) {
    return Flux.fromIterable(idList)
            .flatMapSequential(dependency::fetchOneById)
            .doOnNext(e -> System.out.println("Element: " + e));
  }

  public Flux<A> p03_fetchOneByOne(List<Long> idList) {
    return Flux.fromIterable(idList)
            .concatMap(dependency::fetchOneById)
            .doOnNext(e -> System.out.println("Element: " + e));
  }

  // fetch in pages of size=4;
  // 2: log any error occurred.
  // 3: don't allow any element to wait more than 200 millis
  // 4: make sure any error in the sendMessage does NOT cancel the flux. log the error.
  // 4: make sure any from any upstream operator does NOT cancel the flux. log the error.
  // Nor in the fetchPage
  public void p04_fetchInPages(Flux<Long> infiniteFlux) {
    infiniteFlux
            .buffer(4)
            .flatMap(page -> dependency.fetchPageByIds(page))
            .flatMap(a -> dependency.sendMessage(a))
            .subscribe()
    ;
  }

//  // the ids arrive on
//  public Flux<A> p05_fetchInPages_maxWaitingTime(Flux<Long> infiniteFlux) {
//    return infiniteFlux
//            .bufferTimeout(4, Duration.ofMillis(500))
//            .flatMap(page -> dependency.fetchPageByIds(page));
//  }

  public Mono<Void> p06_ignoreErrors(Flux<Long> infiniteFlux) {
    return infiniteFlux
            .flatMap(id -> dependency.fetchOneById(id))
            .flatMap(a -> dependency.sendMessage(a))
            .onErrorContinue((exception, offendingElement) -> System.out.println("Ignoring exception "+exception + " on element " + offendingElement))
            .then();
  }


  // groupedFlux
  //!! .subscribe pierde ReactorContextul!!

}
