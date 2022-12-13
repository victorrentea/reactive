package victor.training.reactor.workshop;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class P1_Creation {

  //========================================================================================
  // TODO Return a Mono that emits a "foo" value (DATA SIGNAL)
  //  eg when a repo.findById(id) finds data.
  public Mono<String> mono1_just() {
    //    return Mono.just("foo"); // corect
    return Mono.create(sink -> { // nu folosi in prod. doar pt learning.
      sink.success("foo"); // emite semnalul de date + completion
    });
  }

  //========================================================================================
  // TODO Return an empty Mono (COMPLETION SIGNAL)
  //   eg for reporting completion of a task not returning any data
  public Mono<String> mono2_empty() {
    return Mono.empty();
  }

  //========================================================================================
  // TODO Return a Mono of the data passed as parameter.
  //  NOTE: data can come null => if null, emit no DATA SIGNAL, but only COMPLETION.
  public Mono<String> mono3_optional(String data) {
    return Mono.justOrEmpty(data);
  }

  //========================================================================================
  // TODO Create a Mono that completes with an ERROR SIGNAL of IllegalStateException
  public Mono<String> mono4_error() {
    return Mono.error(new IllegalStateException());
  }

  //========================================================================================

  // TODO Return a Mono that never emits any signal (eg for testing)
  public Mono<String> mono5_noSignal() {

    return repoFindById()
            //            .timeout(Duration.ofMillis(400), Mono.justOrEmpty("n-a venit"))
            ;
  }

  private static Mono<String> repoFindById() {
    return Mono.never();
  }

  // TODO ⭐️ CHALLENGE: to understand the signals,
  //   reimplement all the above mono* exercises using Mono.create(..)
  //   [AVOID IN PRODUCTION]


  //========================================================================================
  // TODO Create a Mono that emits "BOO" after 100ms
  public Mono<String> mono6_delayedData() {
    return Mono.just("BOO").delayElement(Duration.ofMillis(100));
    // regula in cod de prod nu mai ai voie sleep() ca e blocant.
  }

  //========================================================================================
  // TODO Create a Mono that emits LocalDateTime.now() when subscribed to.
  // Hint: do NOT use .just() -> now() should be called later, when subscribed!
  public Mono<LocalDateTime> mono7_fromCallable() {
    //    return Mono.just(LocalDateTime.now()); // cheama now() la mom chemarii functiei asteia

    return Mono.fromCallable(() -> LocalDateTime.now()); // acum now() ruleaza
    // doar atunci cand vreun Subscriber se subscrie si cere elementul
  }

  //========================================================================================
  // TODO Create a Mono that emits only completion after 100ms (with no value)
  // NOTE: the return type is Mono<Void>, indicating there is no data emitted to subscriber.
  public Mono<Void> mono8_delayedCompletion() {
    //    Void v = new Void();
    //    Mono.justOrEmpty(Optional.empty()) == Mono.empty()
    return Mono.delay(Duration.ofMillis(100))
            .then();
  }


  //========================================================================================

  // TODO Return a Flux that contains 2 values "foo" and "bar" without using an array or a collection
  public Flux<String> flux1_values() {
    return Flux.just("foo", "bar");
  }

  //========================================================================================

  // TODO Create a Flux from a List that contains 2 values "foo" and "bar"
  public Flux<String> flux2_fromList(List<String> list) {
    return Flux.fromIterable(list);
  }

  //========================================================================================
  // TODO Return an empty Flux
  public Flux<String> flux3_empty() {
    return Flux.empty();
  }

  //========================================================================================
  // TODO Create a Flux that emits an IllegalStateException ERROR SIGNAL
  public Flux<String> flux4_error() {
    return Flux.error(new IllegalStateException());
  }

  //========================================================================================
  // TODO Create a Flux that emits increasing values from 0 to 9 every 100ms
  public Flux<Long> flux5_delayedElements() {
    return Flux.interval(Duration.ofMillis(100)).take(10);
  }

  //========================================================================================
  // TODO print to console all signals going up (from Subscriber->Publisher)
  //  or down (from Publisher->Subscriber)
  public Flux<String> logSignals(Flux<String> flux) {
    return flux
            .log("deasupra") // ⬆subscribe,request  ⬇next,complete,error

            .subscribeOn(Schedulers.single())
            .publishOn(Schedulers.parallel())

            .log("sub") // ⬆subscribe,request  ⬇next,complete,error

            ;
  }

  // Magie: acel lucru pentru care Jr o sa vina la tine sa te intrebe.
  //========================================================================================
  // TODO print to console:
  //  - "SUBSCRIBE" on subscribe signal
  //  - "NEXT "+element on next signal (concatenate the element to message)
  //  - "ERROR" on error signal
  //  - "END" on error or completion signal
  public Flux<String> doOnHooks(Flux<String> flux) {
    return flux;
  }



  //========================================================================================
  // TODO The returned Mono should emit "Hi " + the current username
  //  got from the Reactor Context, eg  context.get("username")
  // Hint: Mono.deferContextual allows access to context propagated from downstream subscriber: inspect the test also.
  // echivalentul in Reactor a ThreadLocal
  // asa merge  @Transactional, propagarea de user de securitate, metadate : Logback MDC %X
  public Mono<String> reactorContext_read() {
    return Mono.deferContextual(reactorContext -> {
              String user = reactorContext.get("user");
              return Mono.just("Hi " + user);
            })

            // pun ei eu pe context chestii accesibile ulterior doar celor "DE MAI SUS" in lant. pe publisherii de mai sus.
            .contextWrite(c -> c.put("si eu", "ceva"))

            ;
  }

}
