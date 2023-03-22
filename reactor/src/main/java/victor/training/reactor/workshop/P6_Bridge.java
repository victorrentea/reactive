package victor.training.reactor.workshop;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.One;
import victor.training.reactor.lite.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
public class P6_Bridge {

  protected static Logger log = LoggerFactory.getLogger(P6_Bridge.class);

  protected static class User {
  }

  @Data
  protected static class ResponseMessage {
    String data;
  }

  @Component
  protected static class Dependency {
    public Mono<String> save(String message) {
      return Mono.just(message);
    }

    public Flux<String> findAll() {
      return Flux.empty();
    }

    public String legacyBlockingCall(){
      Utils.sleep(10); // blocks the caller thread
      return "";
    }

    public void sendMessageOnQueueBlocking(Long id) {
      log.info("Sent message {}", id);
    }
  }

  protected final Dependency dependency;

  public P6_Bridge(Dependency dependency) {
    this.dependency = dependency;
  }


  // ==================================================================================
  // TODO call dependency#save() and block the current thread until it completes, then log.info the returned value
  // Use-Case: block the thread in a @Scheduled / Message Listener, that would otherwise consume a new message (eg Kafka, Rabbit)
  public void p01_blockForMono(String message) {
    // dependency.save(message).subscribe();
    //1) @Scheduled this would NOT block the thread and the @Scheduled will tick again 10 minutes later, starting starting another processing
    //2) if a @RabbitListener methodcompletes w/o error, Rabbit will call it again with the next message
    // = primitive way to do backpressure

    // corect:
    dependency.save(message).block();
  }

  // ==================================================================================
  // TODO call dependency#findAll() and block to get all values, then log.info the list in the console.
  // Use-Case: same as above
  public void p02_blockForFlux() {
    List<String> allItems = null;
    log.info("List: " + allItems);
  }

  // ==================================================================================
  // TODO call dependency#legacyBlockingCall() and return its result wrapped in a Mono.
  //  NOTE: you are only allowed to block threads of Schedulers.boundedElastic()
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
  @GetMapping("message-bridge")
  public Mono<ResponseMessage> p06_messageBridge(@RequestParam(defaultValue = "1") long id) {
    // One method has to create the Mono
    One<ResponseMessage> sink = Sinks.one();
    futureResponse.put(id, sink);
    return sink.asMono();
  }

  private Map<Long, One<ResponseMessage>> futureResponse = new HashMap<>(); // TODO

  // @MessageListener imagine..
  @PostMapping("receive-reply-message") // simulate with a REST api for easier testing.
  public void p06_receiveOnReplyQueue(@RequestParam(defaultValue = "1") long id,
                                      @RequestBody ResponseMessage response) {
    One<ResponseMessage> sink = futureResponse.remove(id);
    sink.tryEmitValue(response);
  }
  // ⭐️ Challenge: Support multiple concurrent requests waiting at the same time






  // ==================================================================================
  // TODO This method is called once by each browser.
  //  Every time the p07_externalSignal(i) method is called later, the flux must emit the 'i' value.
  // Use-Case: report on a Flux signals received as API calls or Messages
  // Hint: use Sinks.???
  @GetMapping(value = "flux-broadcast", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<Integer> p07_fluxBroadcast() {
    return null;
  }

  private Sinks.Many<Integer> fluxSink; // TODO = ...

  @GetMapping("flux-signal")
  public void p07_externalSignal(@RequestParam(defaultValue = "9") Integer data) {
    // TODO write code here to emit the data in the flux returned in the previous method.
  }

}
