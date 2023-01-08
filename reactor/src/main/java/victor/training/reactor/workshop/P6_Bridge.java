package victor.training.reactor.workshop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class P6_Bridge {

  protected Logger log = LoggerFactory.getLogger(P6_Bridge.class);

  protected static class User {
  }

  protected static class ResponseMessage {
  }

  protected interface Dependency {
    Mono<String> save(String message);

    Flux<String> findAll();

    String legacyBlockingCall(); // blocks the caller thread

    void sendMessageOnQueueBlocking(Long id);
  }

  protected final Dependency dependency;

  public P6_Bridge(Dependency dependency) {
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
  // TODO call dependency#sendMessageOnQueueBlocking(id) and then return a Mono
  //  that emits the ResponseMessage received LATER via the next method (aka callback).
  @GetMapping // imagine..
  public Mono<ResponseMessage> p06_callback(long id) {
    return null;
  }

  private Sinks.One<ResponseMessage> futureResponse; // TODO = ...

  // @MessageListener imagine..
  public void p06_receiveResponseOnReplyQueue(long id, ResponseMessage response) {
    // TODO write code here to emit the received response in the Mono returned by the method above

  }
  // ⭐️ Challenge: Can you make this work even if there are 2 pending requests at the same time?


  // ==================================================================================
  // TODO This method is called once. Every time the next method is called, this flux must emit the integer data.
  // Use-Case: report on a Flux signals received as API calls or Messages
  // Hint: use Sinks.???
  public Flux<Integer> p07_fluxOfSignals() {
    return null;
  }

  private Sinks.Many<Integer> fluxSink; // TODO = ...

  public void p07_externalSignal(Integer data) {
    // TODO write code here to emit the data in the flux returned in the previous method.
  }


}
