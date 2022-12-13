package victor.training.reactor.workshop;

import lombok.Value;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerResponse;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class P7_Flux {
  public P7_Flux(Dependency dependency) {
    this.dependency = dependency;
  }
  final Dependency dependency;

  @Value
  static class A{Long id;}
  interface Dependency {
    Mono<A> fetchOneById(Long id);
    Flux<A> fetchPageByIds(List<Long> idPage);
    Mono<Void> sendMessage(A a);

    Mono<Void> sendOdd1(Integer oddMessage);
    Mono<Void> sendOdd2(Integer oddMessage);
    Mono<Void> sendEven(List<Integer> evenMessagePage);
  }


//  private static void copyFileUsingChannel(File source, HttpServerResponse response) throws IOException {
//    FileChannel sourceChannel = null;
//    FileChannel destChannel = null;
//
//    HttpServerResponse r;
//    r.sendFile()  // << asta vrei sau mai bine : spring.webflux.static-path-pattern
//  }

  // ==================================================================================
  // TODO #1 fetch each element by id using .fetchOneById(id)
  // TODO #2 Print elements as they come in. What do you observe? (the original IDs are consecutive)
  // TODO #3 Restrict the concurrency to maximum 4 requests in parallel - huh ??!
//  Mono<List<A>> -- mai rau pentur ca asteapta toate pentru a incepe sa le curga subscriberului

//  private static final Map<String, byte[]>
  @GetMapping
  public Flux<A> p01_fetchInParallel_scrambled(List<Long> idList) {
//    Path path = new File("file.txt").toPath();
    System.out.println("IDs to fetch: "+ idList);
    return Flux.fromIterable(idList)
            // una din cele mai mari greseli cand faci Flux.flatMap este sa nu pui concurrency (param 2)
            .flatMap(id -> dependency.fetchOneById(id),4)
            .doOnNext(System.out::println)
            ;
    // PROBLEMA 1: elementele emise de flatMap pot fi in alta ordine decat cea originala,
    //    daca fetchOneById() returneaza cu latente diferite
    // problema = cand end userul le vrea in aceeasi ordine - RAR

    // PROBLEMA 2: cum eviti sa-l stalcesti cu multe requesturi in paralel pe api-u pe care-l chemi
  }

  // ==================================================================================
  // TODO same as above, but fire all requests in parallel.
  //  Still, preserve the order of the items in the list
  public Flux<A> p02_fetchInParallel_preservingOrder(List<Long> idList) {
    System.out.println("IDs to fetch: "+ idList);
    return Flux.fromIterable(idList)
            .flatMapSequential(id->dependency.fetchOneById(id), 4)
            .doOnNext(System.out::println)
            ;
    // RISK tii in memorie toate rezultatele tutoror ca sa le poti servi in ordine >> Memorie --
  }

  // ==================================================================================
  // TODO same as above, but fire only one request in parallel at a time (thus, still preserve order).
  public Flux<A> p03_fetchOneByOne(List<Long> idList) {
    System.out.println("IDs to fetch: "+ idList);
    return Flux.fromIterable(idList)
//            .flatMapSequential(id->dependency.fetchOneById(id), 1)
            .concatMap(id->dependency.fetchOneById(id))
            .doOnNext(System.out::println)
            ;
    // SLAB pt ca dureaza mult.: timp de completion total MARE ca nu paralelizeaza nimic;
  }

  // toate cele de mai sus au in comun o problema grava,
  // care merita PR reject!! + send un mail...
  // GRAV DE TOT: stai ca ... dupa retea dute-vino dute-vino
  // ex: ai de adus 1000 de ID -> network latency 4 ms x 1000 = 4s de stat ca ...
// ce faci: dezv un API care aduce in pagini: eg 500 - 1000 / pag



  // ==================================================================================
  // TODO #1 to save network latency, fetch items in pages of size=4, using .fetchPageByIds
  // TODO #2 don't allow any ID to wait more than 200 millis  (hint: look for a buffer* variant)
  // TODO #3 limit concurrent request to max 2 in parallel and make sure you don't scramble the elements
  public Flux<A> p04_fetchInPages(Flux<Long> idFlux) {
    return idFlux
//            .buffer(4)
            .bufferTimeout(4,Duration.ofMillis(200))
            .flatMapSequential(dependency::fetchPageByIds, 2);
  }

  // ==================================================================================
  // TODO for any incoming id > 0, .fetchOneById(id) and then send it to .sendMessage(a)
  //  Hint: this method runs at startup of a fictitious app => It has to .subscribe() to the flux!
  // TODO #2 any error in fetchOneById should be logged and the element discarded, but and NOT cancel/stop the flux
  //  Hint: onError...
  // TODO #3 any error (fetch OR send) should be logged and the element discarded, but and NOT cancel/stop the flux
  //  Hint: onErrorContinue
  @PostConstruct
  public void p05_infinite(Flux<Long> infiniteFlux) {
    infiniteFlux

            .filter(id ->  id > 0)

            .concatMap(id -> dependency.fetchOneById(id)
//                    .doOnError(System.err::println)
//                    .onErrorResume(e->Mono.empty()) // merge, e corecta, dar daca apare dintr-un alt operator de pe flxul infinit o eroare?
            )

            .flatMap(a -> dependency.sendMessage(a))

            // cu ff multa magie o sa inghit orice exceptie care sare de ORIUNDE
            // de mai sus in chain, o loga si ma voi preface ca nimic nu s-a intamplat
            // bagam gunoiu sub pres.
            .onErrorContinue((exception, offendingElement) -> {
              System.err.println("Elementul " + offendingElement + " a crapat cu eroare: "+ exception);
            })
            .subscribe(); // <- the only legal place
  }

  // ==================================================================================
  // TODO #1 for any incoming element < 0, increment a counter and emit its value
  ///        pe java8 Streams: reduce(0, (acc, e) -> )
  //   eg for input 1, -1, 2, -3, 0, -1  =>output=> 0, 1, 1, 2, 2, 3
  //   in other words emit how many negative elements were seen by now
  // TODO #2 do NOT emit repeated values:
  //   eg for the input above =>output=> 0, 1, 2, 3

  public Flux<Integer> p06_monitoring(Flux<Integer> flux) {
    return flux.scan(0, (acc, e) -> acc + (e<0?1:0))
            .distinctUntilChanged()
//            .filter(bine->nasol)
            ;
  }

  // ==================================================================================
  // TODO based on the MessageType.forMessage(int) below, do one of the following:
  //  - TYPE1_NEGATIVE: Do nothing (ignore the message)
  //  - TYPE2_ODD: Call .sendOdd1(message) and .sendOdd2(message) in parallel
  //  - TYPE3_EVEN: Call .sendEven(List.of(message))
  //  Then, to optimize network traffic, buffer together messages sent to .sendEven(page),
  //   in pages of max 3 items, but without having any item waiting more than 200 millis
  // Bonus: debate .buffer vs .window
  public Mono<Void> p09_groupedFlux(Flux<Integer> infiniteMessageStream) {
    return infiniteMessageStream
            .groupBy(MessageType::forMessage)
            .flatMap(groupedFlux -> {
              switch (groupedFlux.key()) {
                case TYPE1_NEGATIVE: return Mono.empty();
                case TYPE2_ODD: return groupedFlux
                        .flatMap(odd ->Mono.zip(dependency.sendOdd1(odd), dependency.sendOdd2(odd)));
                case TYPE3_EVEN:
                  return groupedFlux
                          .bufferTimeout(3, Duration.ofMillis(200))
                          .flatMap(page -> dependency.sendEven(page));
                default:
                  return Flux.error(new IllegalStateException("Unexpected value: " + groupedFlux.key()));
              }
            })
            .then();
  }

  enum MessageType {
    TYPE1_NEGATIVE,
    TYPE2_ODD,
    TYPE3_EVEN;

    public static MessageType forMessage(Integer message) {
      if (message < 0) return TYPE1_NEGATIVE;
      if (message % 2 == 1) return TYPE2_ODD;
      else return TYPE3_EVEN;
    }
  }


}


