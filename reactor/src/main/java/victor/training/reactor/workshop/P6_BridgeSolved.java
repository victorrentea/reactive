package victor.training.reactor.workshop;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.Many;
import reactor.core.publisher.Sinks.One;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.concurrent.CompletableFuture;

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

  public Mono<ResponseMessage> p06_callback(long id) {
    return Mono.fromRunnable(() ->dependency.sendMessageOnQueueBlocking(id))
            .subscribeOn(Schedulers.boundedElastic())
            .then(futureResponse.asMono());
  }

  private final One<ResponseMessage> futureResponse = Sinks.one();

  public void p06_receiveResponseOnReplyQueue(long id, ResponseMessage response) {
    futureResponse.tryEmitValue(response);
  }

  public Flux<Integer> p07_fluxOfSignals() {
    return fluxSink.asFlux();
  }

  private final Many<Integer> fluxSink = Sinks.many().unicast().onBackpressureBuffer();

  public void p07_externalSignal(Integer data) {
    fluxSink.tryEmitNext(data);
  }
}
