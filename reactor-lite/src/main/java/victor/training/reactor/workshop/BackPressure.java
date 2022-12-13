package victor.training.reactor.workshop;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
public class BackPressure {
  public static void main(String[] args) {
    //https://stackoverflow.com/questions/57296097/how-does-backpressure-work-in-project-reactor
//    System.out.println("------ Cold publisher that respects the request(n) from subscriber");
//    // asa ar fi corect!!
//    Flux.range(1, Integer.MAX_VALUE)
//            .log()
//            .concatMap(x -> Mono.delay(Duration.ofMillis(100)), 2) // simulate that processing takes time
//            .take(4)
//            .blockLast();



//    System.out.println("------ Cold publisher that does NOT respect the request(n) from subscriber");
//    Flux.interval(Duration.ofMillis(50)) // impinge la fiecare 50 ms cate un element in jos... FARA sa-i pese de cate ai cerut,
////            .onBackpressureBuffer()
//            .onBackpressureDrop()
//            .log()
//            .concatMap(x -> Mono.delay(Duration.ofMillis(100)), 1) // simulate that processing takes time
//            .take(4)
//            .blockLast();


//    ConnectableFlux<Long> hotFlux = Flux.interval(Duration.ofMillis(10))
//            .doOnNext(e -> log.info("Emit "+e))
//            .onBackpressureDrop()
//            .publish();
//    hotFlux.connect();
//
//    System.out.println("Subscriber#1");
//    hotFlux.log()
//            .concatMap(x -> Mono.delay(Duration.ofMillis(30)), 1) // simulate that processing takes time
//            .take(4)
//            .blockLast();
//
//    System.out.println("Subscriber#2");
//    hotFlux.log()
//            .concatMap(x -> Mono.delay(Duration.ofMillis(30)), 1) // simulate that processing takes time
////            .take(4)
//            .blockLast();


  }

}
