package victor.training.reactor.workshop;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.time.Duration.ofMillis;

public class P1_Creation {

  //========================================================================================
  // TODO Return a Mono that emits a "foo" value (DATA SIGNAL)
  //  eg when a repo.findById(id) finds data.
  public Mono<String> mono1_just() {
    return Mono.just("foo");
  }

  //========================================================================================
  // TODO Return an empty Mono (COMPLETION SIGNAL)
  //   eg for reporting completion of a task not returning any data
  public Mono<Void> mono2_empty() {
    return Mono.empty();
  }

  //========================================================================================
  // TODO Return a Mono of the data passed as parameter.
  //  NOTE: data can come null => if null, emit no DATA SIGNAL, but only COMPLETION.
  public Mono<String> mono3_optional(String data) {
    return Mono.justOrEmpty(data); // Mono.empty = Optional.empty
  }

  //========================================================================================
  // TODO Create a Mono that completes with an ERROR SIGNAL of IllegalStateException
  public Mono<String> mono4_error() {
    return Mono.error(new IllegalStateException());
    // daca o metoda intoarce Mono/Flux NU ARE VOIE sa faca nici CATCH/THROW
    // ci trebuie sa intorci exceptia intr-un mono
  }

  //========================================================================================

  // TODO Return a Mono that never emits any signal (eg for testing)
  public Mono<String> mono5_noSignal() {
    return Mono.never();
  }

  // TODO ⭐️ CHALLENGE: to understand the signals,
  //   reimplement all the above mono* exercises using Mono.create(..)
  //   [AVOID IN PRODUCTION]


  //========================================================================================
  // TODO Create a Mono that emits "BOO" after 100ms
  public Mono<String> mono6_delayedData() {
    return Mono.just("BOO")
        .delayElement(ofMillis(100));
  }

  //========================================================================================
  // TODO Create a Mono that emits LocalDateTime.now() when subscribed to.
  // Hint: do NOT use .just() -> now() should be called later, when subscribed!
  public Mono<LocalDateTime> mono7_fromCallable() {
//    return Mono.just(LocalDateTime.now()); // va emite mereu timpul la momentul chemarii functiei asteia
    return Mono.fromCallable(()->LocalDateTime.now());
  }

  //========================================================================================
  // TODO Create a Mono that emits only completion after 100ms (with no value)
  // NOTE: the return type is Mono<Void>, indicating there is no data emitted to subscriber.
  public Mono<Void> mono8_delayedCompletion() {
    return Mono.just("yo")
        .delayElement(ofMillis(100))
        .then() // iei doar semnalu de final
        ;
  }


  //========================================================================================

  // TODO Return a Flux that contains 2 values "foo" and "bar"
  //   (without using an array or a collection)
  public Flux<String> flux1_values() {
    return Flux.just("foo", "bar");
  }

  //========================================================================================

  // TODO Create a Flux from a List that contains 2 values "foo" and "bar"
  public Flux<String> flux2_fromList(List<String> ids) {
    return Flux.fromIterable(ids);
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
  // TODO Create a Flux that emits increasing values
  //  from 0 to 9 every 100ms
  public Flux<Long> flux5_delayedElements() {
//    List<Integer> zece = IntStream.iterate(0, i -> i + 1)
//        .limit(10)
//        .collect(Collectors.toList());
//    return Flux.interval(ofMillis(100))
//        .take(10);
    return Flux.range(0,10)
        .delayElements(ofMillis(100))
        .map(i->(long)i);
  }

  //========================================================================================
  // TODO print to console all signals
  //  going up (from Subscriber->Publisher)
  //  or down (from Publisher->Subscriber)
  public Flux<String> logSignals(Flux<String> flux) {
    return flux.log();
  }

  //========================================================================================
  // TODO print to console:
  //  - "SUBSCRIBE" on subscribe signal
  //  - "NEXT "+element on next signal (concatenate the element to message)
  //  - "ERROR" on error signal
  //  - "END" on error or completion signal
  public Flux<String> doOnHooks(Flux<String> flux) {
    return flux
        .doOnSubscribe(s-> System.out.println("SUBSCRIBE"))
        .doOnNext(e -> System.out.println("NEXT " +e))
        .doOnError(e -> System.out.println("ERROR"))
//        .doOnComplete(() -> System.out.println("END"))
        .doOnTerminate(() -> System.out.println("END"))
        ;
  }



  // ReactorContext (specific reactor) e echivalentul ThreadLocal pentru propagarea
  // metadatelor de request: SecurityContext(AccesToken), TraceID (Sleuth), @Transactional, onErrorContinue
  //========================================================================================
  // TODO The returned Mono should emit "Hi " + the current username
  //  got from the Reactor Context, eg  context.get("username")
  // Hint: Mono.deferContextual allows access to context propagated from downstream subscriber: inspect the test also.
  public Mono<String> reactorContext_read() {
    return Mono.deferContextual(contextView -> Mono.just("Hi " + contextView.get("username")));
  }

}
