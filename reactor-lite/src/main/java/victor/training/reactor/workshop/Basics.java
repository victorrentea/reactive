package victor.training.reactor.workshop;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class Basics {

  //========================================================================================

  // TODO Return an empty Mono
  public Mono<String> emptyMono() {
    return null;
  }

  //========================================================================================

  // TODO Return a Mono that never emits any signal (for testing)
  public Mono<String> monoWithNoSignal() {
    return null;
  }

  //========================================================================================

  // TODO Return a Mono that emits a "foo" value
  public Mono<String> fooMono() {
    return null;
  }

  //========================================================================================

  // TODO Return a Mono of data passed as parameter. NOTE: data can come null.
  public Mono<String> optionalMono(String data) {
    return null;
  }

  //========================================================================================

  // TODO Create a Mono that completes with an error signal of IllegalStateException
  public Mono<String> errorMono() {
    return null;
  }

  //========================================================================================
  // TODO Create a Mono that emits only completion after 100ms (with no value)
  public Mono<Void> delayedCompletion() {
    return Mono.empty();
  }

  //========================================================================================
  // TODO Return an empty Flux
  public Flux<String> emptyFlux() {
    return null;
  }

  //========================================================================================

  // TODO Return a Flux that contains 2 values "foo" and "bar" without using an array or a collection
  public Flux<String> fooBarFluxFromValues() {
    return null;
  }

  //========================================================================================

  // TODO Create a Flux from a List that contains 2 values "foo" and "bar"
  public Flux<String> fluxFromList(List<String> list) {
    return null;
  }

  //========================================================================================

  // TODO Create a Flux that emits an IllegalStateException
  public Flux<String> errorFlux() {
    return null;
  }

  //========================================================================================

  // TODO print to console all signals going up (from subscriber) or down (from producer)
  public Flux<String> signals(Flux<String> flux) {
    return flux;
  }

  //========================================================================================

  // TODO Create a Flux that emits increasing values from 0 to 9 every 100ms
  public Flux<Long> countEach100ms() {
    return null;
  }

}
