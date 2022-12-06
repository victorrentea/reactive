package victor.training.reactive.demo.throttle;

import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Scanner;

public class RequestLimit_BackPressure {
  public static void main(String[] args) {

    Flux.interval(Duration.ofMillis(100)).take(100)// imitate a publisher that produces elements at a certain rate

            .log()

            .limitRate(10) // used to requests up to 10 elements from the publisher

            //          .buffer(10) // groups integers by 10 elements, see request(6) on the marble

            .delayElements(Duration.ofSeconds(2)) // emits a group of ints every 2 sec
            .subscribe(System.out::println);

    new Scanner(System.in).nextLine();
  }
}
