package victor.training.reactor.workshop;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

public class P1_Creation {

  //========================================================================================
  // TODO Return a Mono that emits a "foo" value (DATA SIGNAL)
  //  eg when a repo.findById(id) finds data.
  public Mono<String> mono1_just() {
    return null;
  }

  //========================================================================================
  // TODO Return an empty Mono (COMPLETION SIGNAL)
  //   eg for reporting completion of a task not returning any data
  public Mono<String> mono2_empty() {
    return null;
  }

  //========================================================================================
  // TODO Return a Mono of the data passed as parameter.
  //  NOTE: data can come null => if null, emit no DATA SIGNAL, but only COMPLETION.
  public Mono<String> mono3_optional(String data) {
    return null;
  }

  //========================================================================================
  // TODO Create a Mono that completes with an ERROR SIGNAL of IllegalStateException
  public Mono<String> mono4_error() {
    return null;
  }

  //========================================================================================

  // TODO Return a Mono that never emits any signal (eg for testing)
  public Mono<String> mono5_noSignal() {
    return null;
  }

  // TODO ⭐️ CHALLENGE: to understand the signals,
  //   reimplement all the above mono* exercises using Mono.create(..)
  //   [AVOID IN PRODUCTION]


  //========================================================================================
  // TODO Create a Mono that emits "BOO" after 100ms
  public Mono<String> mono6_delayedData() {
    return Mono.just("BOO");
  }

  //========================================================================================
  // TODO Create a Mono that emits LocalDateTime.now() when subscribed to.
  // Hint: do NOT use .just() -> now() should be called later, when subscribed!
  public Mono<LocalDateTime> mono7_fromCallable() {
    return Mono.just(LocalDateTime.now());
  }

  //========================================================================================
  // TODO Create a Mono that emits only completion after 100ms (with no value)
  // NOTE: the return type is Mono<Void>, indicating there is no data emitted to subscriber.
  public Mono<Void> mono8_delayedCompletion() {
    return Mono.empty();
  }


  //========================================================================================

  // TODO Return a Flux that contains 2 values "foo" and "bar" without using an array or a collection
  public Flux<String> flux1_values() {
    return null;
  }

  //========================================================================================

  // TODO Create a Flux from a List that contains 2 values "foo" and "bar"
  public Flux<String> flux2_fromList(List<String> list) {
    return null;
  }

  //========================================================================================
  // TODO Return an empty Flux
  public Flux<String> flux3_empty() {
    return null;
  }

  //========================================================================================
  // TODO Create a Flux that emits an IllegalStateException ERROR SIGNAL
  public Flux<String> flux4_error() {
    return null;
  }

  //========================================================================================
  // TODO Create a Flux that emits increasing values from 0 to 9 every 100ms
  public Flux<Long> flux5_delayedElements() {
    return null;
  }

  //========================================================================================
  // TODO print to console all signals going up (from Subscriber->Publisher)
  //  or down (from Publisher->Subscriber)
  public Flux<String> logSignals(Flux<String> flux) {
    return flux;
  }

}
