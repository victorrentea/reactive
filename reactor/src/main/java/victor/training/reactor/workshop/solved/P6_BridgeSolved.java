package victor.training.reactor.workshop.solved;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.Many;
import reactor.core.publisher.Sinks.One;
import reactor.core.scheduler.Schedulers;
import victor.training.reactor.workshop.P6_Bridge;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
public class P6_BridgeSolved extends P6_Bridge {
  public P6_BridgeSolved(Dependency dependency) {
    super(dependency);
  }

  public void p01_blockForMono(String message) {
    String result = dependency.save(message).block();
    log.info(result);
  }

  public void p02_blockForFlux() {
    List<String> list = dependency.findAll().collectList().block();
    log.info("List:" + list);
  }

  public Mono<String> p03_blockingCalls() {
    return Mono.fromCallable(() -> dependency.legacyBlockingCall())
            .subscribeOn(Schedulers.boundedElastic());
  }

  public CompletableFuture<User> p04_fromMonoToCompletableFuture(Mono<User> mono) {
    return mono.toFuture();
  }

  public Mono<User> p05_fromCompletableFutureToMono(CompletableFuture<User> future) {
    return Mono.fromFuture(future);
  }

  @GetMapping("message-bridge")
  public Mono<ResponseMessage> p06_messageBridge(@RequestParam(defaultValue = "1") long id) {
    return Mono.fromRunnable(() ->dependency.sendMessageOnQueueBlocking(id))
            .subscribeOn(Schedulers.boundedElastic())
            .then(futureResponse.asMono());
  }

  private final One<ResponseMessage> futureResponse = Sinks.one();

  @PostMapping("receive-reply-message")
  public void p06_receiveOnReplyQueue(@RequestParam(defaultValue = "1") long id,
                                      @RequestBody ResponseMessage response) {
    futureResponse.tryEmitValue(response);
  }

  @GetMapping(value = "flux-broadcast", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<Integer> p07_fluxBroadcast() {
    return fluxSink.asFlux();
  }

  private final Many<Integer> fluxSink = Sinks.many().multicast().onBackpressureBuffer();

  @GetMapping("flux-signal")
  public void p07_externalSignal(@RequestParam(defaultValue = "9") Integer data) {
    fluxSink.tryEmitNext(data);
  }
}
