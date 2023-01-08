package victor.training.reactor.workshop.solved;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.One;
import reactor.util.function.Tuples;
import victor.training.reactor.workshop.P7_Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static java.time.Duration.ofMillis;
import static java.util.stream.Collectors.toMap;

public class P7_FluxSolved extends P7_Flux {
  public P7_FluxSolved(P7_Flux.Dependency dependency) {
    super(dependency);
  }
  public Flux<A> p01_fetchInParallel_scrambled(List<Integer> idList) {
    System.out.println("IDs to fetch: "+ idList);
    return Flux.fromIterable(idList)
            .flatMap(dependency::fetchOneById, 4)
            .doOnNext(System.out::println)
            ;
  }

  public Flux<A> p02_fetchInParallel_preservingOrder(List<Integer> idList) {
    return Flux.fromIterable(idList)
            .flatMapSequential(dependency::fetchOneById)
            .doOnNext(System.out::println);
  }

  public Flux<A> p03_fetchOneByOne(List<Integer> idList) {
    return Flux.fromIterable(idList)
            .concatMap(dependency::fetchOneById)
            .doOnNext(System.out::println);
  }

  public Flux<A> p04_fetchInPages(Flux<Integer> flux) {
    return flux
            .bufferTimeout(4, ofMillis(200))
            .flatMapSequential(page -> dependency.fetchPageByIds(page), 2);
  }

  public void p05_infinite(Flux<Integer> infiniteFlux) {
    infiniteFlux
            .filter(id -> id > 0)
            .flatMap(id -> dependency.fetchOneById(id))
            .flatMap(a -> dependency.sendMessage(a))
            .onErrorContinue((exception, element) -> System.out.println("Failed for " + element + " : " + exception))
            .subscribe()
    ;
  }

  public void p06_configureRequestFlux() {
    requests.asFlux()
//            .log("request")
            .bufferTimeout(4, ofMillis(200))
            .map(idPage -> idPage.stream().collect(toMap(Request::getId, Request::getPromise)))
            .flatMap(idPageMap -> dependency.fetchPageByIds(new ArrayList<>(idPageMap.keySet()))
                    .map(a -> Tuples.of(a, idPageMap.get(a.getId())))
            )
            .log("results")
            .doOnNext(tuple -> tuple.getT2().tryEmitValue(tuple.getT1()).orThrow())
            .onErrorContinue((ex, element)-> System.out.println(element + " failed with " +ex))
            .subscribe();

  }


  public Flux<Integer> p07_monitoring(Flux<Integer> flux) {
    return flux
            .scan(0, (acc, e)-> acc + (e<0?1:0))
            .distinctUntilChanged()
            ;
  }

  public Mono<Void> p09_groupedFlux(Flux<Integer> messageStream) {
    return messageStream
            .groupBy(MessageType::forMessage)
            .flatMap(gf ->
            {
              switch (gf.key()) {
                case TYPE1_NEGATIVE: return Mono.empty();
                case TYPE2_ODD: return gf.flatMap(e -> Mono.zip(dependency.sendOdd1(e), dependency.sendOdd2(e)));
                case TYPE3_EVEN: return gf.bufferTimeout(3, Duration.ofMillis(200))
                        .flatMap(dependency::sendEven);
                default: return Flux.error(new IllegalStateException("Unexpected value: " + gf.key()));
              }
            }            )
            .then()
            ;
  }
}


