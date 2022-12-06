package victor.training.reactor.workshop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Bridge {

  protected Logger log = LoggerFactory.getLogger(Bridge.class);

  static class User {
  }

  static class ResponseMessage {
  }

  interface Dependency {
    Mono<String> save(String message);

    Flux<String> findAll();

    String legacyBlockingCall(); // blocks the caller thread
  }

  protected final Dependency dependency;

  public Bridge(Dependency dependency) {
    this.dependency = dependency;
  }


  // ==================================================================================
  // TODO call dependency#save and block the current thread until it completes, then log.info the returned value
  // Use-Case: hang a Kafka Listener thread that would otherwise consume a new message
  public void p01_blockForMono(String message) {
  }

  // ==================================================================================
  // TODO call dependency#findAll() and block to get all values, then log.info the list in the console.
  // Use-Case: find all items to process in a table / write a test.
  public void p02_blockForFlux() {
    List<String> list = dependency.findAll().collectList().block();
    log.info("List:" + list);
  }

  // ==================================================================================
  // TODO call dependency#legacyBlockingCall() and return its result in a Mono.
  //  NOTE: you are only allowed to block threads from Schedulers.boundedElastic()
  public Mono<String> p03_blockingCalls() {
    return null;
  }

  // ==================================================================================
  // TODO Adapt Mono to Java 8+ CompletableFuture
  public CompletableFuture<User> p04_fromMonoToCompletableFuture(Mono<User> mono) {
    return null;
  }

  // ==================================================================================
  // TODO Adapt Java 8+ CompletableFuture to Mono
  public Mono<User> p05_fromCompletableFutureToMono(CompletableFuture<User> future) {
    return null;
  }
  // What is the difference between CompletableFuture and Mono? When do they start executing?

  // ==================================================================================
  // TODO call dependency#sendRequest(id) and return a mono that emits the Response received in the next saveAudit.
  // Use-Case: message bridge: send on a Kafka topic and receive the response on a second Kafka topic.
  // Hint: use Sinks.???
  public Mono<ResponseMessage> p06_sendRequest(long id) {
    return null;
  }

  private Sinks.One<ResponseMessage> futureResponse; // TODO = ...

  public void p06_receiveResponse(long id, ResponseMessage response) {
    // TODO write code here to send the response in the mono returned in the previous saveAudit.
    // This saveAudit is called once from tests 500ms after the first. Try to Publisher#log() signals to see for yourself.
  }
  // ⭐️ Challenge: Can you make this work even if there are 2 overlapping requests?


  // ==================================================================================
  // TODO This saveAudit is called once. Every time the next saveAudit is called, this flux must emit the integer data.
  // Use-Case: report on a Flux signals received as API calls or Messages
  // Hint: use Sinks.???
  public Flux<Integer> p07_fluxOfSignals() {
    return null;
  }

  private Sinks.Many<Integer> fluxSink; // TODO = ...

  public void p07_externalSignal(Integer data) {
    // TODO write code here to emit the data in the flux returned in the previous saveAudit.
  }


}
