package victor.training.reactor.workshop;

import lombok.Value;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;

public class P7_Flux {
  public P7_Flux(Dependency dependency) {
    this.dependency = dependency;
  }
  final Dependency dependency;

  @Value
  static class A{Long id;}
  interface Dependency {
    Mono<A> fetchOneById(Long id);
    Flux<A> fetchPageByIds(List<Long> idPage);
    Mono<Void> sendMessage(A a);

    Mono<Void> sendOdd1(Integer oddMessage);
    Mono<Void> sendOdd2(Integer oddMessage);
    Mono<Void> sendEven(List<Integer> evenMessagePage);
  }

  // ==================================================================================
  // TODO #1 fetch each element by id using .fetchOneById(id)
  // TODO #2 Print elements as they come in. What do you observe? (the original IDs are consecutive)
  // TODO #3 Restrict the concurrency to maximum 4 requests in parallel
  public Flux<A> p01_fetchInParallel_scrambled(List<Long> idList) {
    System.out.println("IDs to fetch: "+ idList);
    return Flux.empty(); // Flux.fromIterable(idList)...
  }

  // ==================================================================================
  // TODO same as above, but fire all requests in parallel.
  //  Still, preserve the order of the items in the list
  public Flux<A> p02_fetchInParallel_preservingOrder(List<Long> idList) {
    return Flux.empty();
  }

  // ==================================================================================
  // TODO same as above, but fire only one request in parallel at a time (thus, still preserve order).
  public Flux<A> p03_fetchOneByOne(List<Long> idList) {
    return Flux.empty();
  }

  // ==================================================================================
  // TODO #1 to save network latency, fetch items in pages of size=4, using .fetchPageByIds
  // TODO #2 don't allow any ID to wait more than 200 millis  (hint: look for a buffer* variant)
  // TODO #3 limit concurrent request to max 2 in parallel and make sure you don't scramble the elements
  public Flux<A> p04_fetchInPages(Flux<Long> infiniteFlux) {
    return Flux.empty();
  }

  // ==================================================================================
  // TODO for any incoming id > 0, .fetchOneById(id) and then send it to .sendMessage(a)
  //  Hint: this method runs at startup of a fictitious app => It has to .subscribe() to the flux!
  // TODO #2 any error in fetchOneById should be logged and the element discarded, but and NOT cancel/stop the flux
  //  Hint: onError...
  // TODO #3 any error (fetch OR send) should be logged and the element discarded, but and NOT cancel/stop the flux
  //  Hint: onErrorContinue
  @PostConstruct
  public void p05_infinite(Flux<Long> infiniteFlux) {
    // .subscribe(); // <- the only legal place ?
  }

  // ==================================================================================
  // TODO #1 for any incoming element < 0, increment a counter and emit its value
  //   eg for input 1, -1, 2, -3, 0, -1  =>output=> 0, 1, 1, 2, 2, 3
  //   in other words emit how many negative elements were seen by now
  // TODO #2 do NOT emit repeated values:
  //   eg for the input above =>output=> 0, 1, 2, 3

  public Flux<Integer> p06_monitoring(Flux<Integer> flux) {
    return Flux.empty();
  }

  // ==================================================================================
  // TODO based on the MessageType.forMessage(int) below, do one of the following:
  //  - TYPE1_NEGATIVE: Do nothing (ignore the message)
  //  - TYPE2_ODD: Call .sendOdd1(message) and .sendOdd2(message) in parallel
  //  - TYPE3_EVEN: Call .sendEven(List.of(message))
  //  Then, to optimize network traffic, buffer together messages sent to .sendEven(page),
  //   in pages of max 3 items, but without having any item waiting more than 200 millis
  // Bonus: debate .buffer vs .window
  public Mono<Void> p09_groupedFlux(Flux<Integer> messageStream) {
    return messageStream
            // .groupBy(MessageType::forMessage)
            .then();
  }

  enum MessageType {
    TYPE1_NEGATIVE,
    TYPE2_ODD,
    TYPE3_EVEN;

    public static MessageType forMessage(Integer message) {
      if (message < 0) return TYPE1_NEGATIVE;
      if (message % 2 == 1) return TYPE2_ODD;
      else return TYPE3_EVEN;
    }
  }


}


