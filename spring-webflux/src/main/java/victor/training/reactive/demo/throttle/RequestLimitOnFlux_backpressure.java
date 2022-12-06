package victor.training.reactive.demo.throttle;

import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Scanner;

public class RequestLimitOnFlux_backpressure {
  public static void main(String[] args) {

    Flux.interval(Duration.ofMillis(100))
            .take(100)
//                  .onBackpressureBuffer(100)
            .log()

                   .limitRate(10,5) // used to requests up to 10 elements from the publisher
//            .buffer(2) // groups integers by 10 elements

            .delayElements(Duration.ofSeconds(2)) // emits a group of ints every 2 sec
            .subscribe(System.out::println);

    new Scanner(System.in).nextLine();
  }
}
