package victor.training.reactor.workshop;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.One;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;

@Slf4j
public class C7_Flux {
  protected Dependency dependency;

  public C7_Flux(Dependency dependency) {
    this.dependency = dependency;
  }

  @Value
  protected static class A {
    int id;
  }

  protected interface Dependency {
    Mono<A> fetchOneById(Integer id);

    Flux<A> fetchPageByIds(List<Integer> idPage);

    Mono<Void> sendMessage(A a);

    Mono<Void> sendOdd1(Integer oddMessage);

    Mono<Void> sendOdd2(Integer oddMessage);

    Mono<Void> sendEven(List<Integer> evenMessagePage);
  }

  // ==================================================================================
  // TODO #1 fetch each element by id using .fetchOneById(id)
  // TODO #2 Print elements as they come in. What do you observe? (the original IDs are consecutive)
  // TODO #3 Restrict the concurrency to maximum 4 requests in parallel
  public Flux<A> p01_fetchMany(List<Integer> idList) {
    System.out.println("IDs to fetch: " + idList);
    var result = Flux.fromIterable(idList)
//        .flatMap(id -> dependency.fetchOneById(id).log("call")) // 256 parallel calls hapen
        .log("main-flow")
        .flatMap(id -> someCall(id).log("inner"), 3, 4)
        // no more than 2 monos subscribed at once
        // max 4 elements prefetched from above
//        .concatMap(id -> dependency.fetchOneById(id)) //only ask for 10 in advance
        // 😊preserves-order 🙁slow 🙁incoming-elements-from-above-mightOOME (max=32 in advance)
//        .flatMapSequential(id -> dependency.fetchOneById(id),2,3)
        // 😊faster(ASAP calls) 😊order-preserved 🙁if 1st call is slow, all later buffer their response

        .doOnNext(e -> log.info("Elem " + e));
    ;
    //All the above is bad en engineering, because we are calling over network in a loop, id by id, in parallel
    // ideally🦄 I dream of a bulk retrieve all IDs at once.
    // what's the max page size??
    return result;
  }

  private Mono<A> someCall(Integer id) {
    return dependency.fetchOneById(id) // MONO
//          .transform(Bulkhead.concurrency(8) resilience4j)
        ;
  }

  // ==================================================================================
  // TODO same as above, but fire all requests in parallel.
  //  Still, preserve the order of the items in the list
  public Flux<A> p02_fetchInParallel_preservingOrder(List<Integer> idList) {
    return Flux.fromIterable(idList)
        .flatMapSequential(id -> dependency.fetchOneById(id))
        ;
  }

  // ==================================================================================
  // TODO same as above, but fire only one request in parallel at a time (thus, still preserve order).
  public Flux<A> p03_fetchOneByOne(List<Integer> idList) {
    return Flux.fromIterable(idList)
        .concatMap(id -> dependency.fetchOneById(id))
//        .flatMap(id -> dependency.fetchOneById(id),1)
        ;
  }


  // ==================================================================================
  // TODO #1✅ to save network latency, fetch items in pages of size=4, using .fetchPageByIds
  // TODO #2✅ don't allow any ID to wait more than 200 millis to be sent out (hint: look for a buffer* variant)
  // TODO #3 limit concurrent request to max 2 in parallel and make sure you don't scramble the elements
  public Flux<A> p04_fetchInPages(Flux<Integer> idFlux) {
    return idFlux
//        .buffer(4)
        .bufferTimeout(4, Duration.ofMillis(200))
        .flatMapSequential(idPage -> dependency.fetchPageByIds(idPage), 2);
  }

  // ==================================================================================
  // ✅#1 for any incoming id > 0, .fetchOneById(id) and then send it to .sendMessage(a)
  // TODO #2 any error in fetchOneById should be logged✅ and the element discarded, but DO NOT cancel/stop the flux
  //  Hint: onError...
  // TODO #3 any error (fetch OR send) should be logged and the element discarded, but DO NOT cancel/stop the flux
  //  Hint: onErrorContinue

//  @PostConstruct // !!!⭐️⭐✅ but ugly🙁
//  public void p05_infinite(Flux<Integer> infiniteFlux) { // kafka topic, rabbit queue
//    infiniteFlux
//        .filter(id -> id > 0)
//        .flatMap(id -> dependency.fetchOneById(id)
//            .doOnError(ex -> log.error("booboo" + ex))
//            .onErrorResume(ex -> Mono.empty()))
//        .flatMap(a -> dependency.sendMessage(a)
//            .doOnError(ex -> log.error("booboo" + ex))
//            .onErrorResume(ex -> Mono.empty())
//        )
//        .subscribe(); //⭐️ <- the only safe place ?
//    // at startup fire in background the processing of messages, and let it run forever
//  }
  @PostConstruct // !!!⭐️
  public void p05_infinite(Flux<Integer> infiniteFlux) { // kafka topic, rabbit queue
    infiniteFlux
        .filter(id -> id > 0)
        .flatMap(id -> dependency.fetchOneById(id))
        .flatMap(a -> dependency.sendMessage(a))

        //aspect all above operators with a try { } cathch. magic, but must-have in infinite fluxes
        .onErrorContinue((ex,previousElement)->log.info("Elem "+previousElement+" failed with "+ex))

        .subscribe(); //⭐️ <- the only safe place ?
    // at startup fire in background the processing of messages, and let it run forever
  }


  // ==================================================================================
  // TODO Batch requests together in pages of max 4 items, each element waiting max 200ms to be sent (bufferTimeout).
  //  when a page of results comes back, complete the respective opened Mono<>
  // Any call to submit request is returned a MOno that is completed later when the item in the page returns
  // WARNING: EXTRA-EXTRA-EXTRA HARD
  @Value
  protected static class Request {
    int id;
    One<A> promise;
  }

  protected Sinks.Many<Request> requests = Sinks.many().unicast().onBackpressureBuffer();

  public void p06_configureRequestFlux() {
    requests.asFlux()
        // TODO
        //dependency.fetchPageByIds(idPage)
        //request.getPromise().tryEmitValue(a)
        .subscribe();

  }

  public Mono<A> p06_submitRequest(int id) {
    One<A> promise = Sinks.one();
    requests.tryEmitNext(new Request(id, promise));
    return promise.asMono();
  }


  // ==================================================================================
  // TODO #1 for any incoming element < 0, increment a counter and emit its value
  //   eg for input 1, -1, 2, -3, 0, -1  =>output=> 0, 1, 1, 2, 2, 3
  //   in other words emit how many negative elements were seen by now
  // TODO #2 do NOT emit repeated values:
  //   eg for the input above =>output=> 0, 1, 2, 3

  public Flux<Integer> p07_monitoring(Flux<Integer> flux) {
    return Flux.empty();
  }


  // ==================================================================================
  // TODO based on the MessageType.forMessage(int) below, do one of the following:
  //  - TYPE1_NEGATIVE: Do nothing (ignore the message)
  //  - TYPE2_ODD: Call .sendOdd1(message) and .sendOdd2(message) in parallel
  //  - TYPE3_EVEN: Call .sendEven(List.of(oneMessage))
  //  - TYPE3_EVEN: Call .sendEven(pageOfMessages) <- ⭐️⭐️⭐️ HARD
  //      * to optimize network traffic send in pages of size = 3
  //      * avoid delaying an element by more than 200 millis
  // Bonus: debate .buffer vs .window
  public Mono<Void> p09_groupedFlux(Flux<Integer> messageStream) {
    return messageStream
        // .groupBy(MessageType::forMessage)
        .then();
  }

  protected enum MessageType {
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


