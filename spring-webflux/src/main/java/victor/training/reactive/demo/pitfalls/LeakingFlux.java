package victor.training.reactive.demo.pitfalls;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.publisher.TestPublisher;

public class LeakingFlux {

   private TestPublisher<String> coldPublisher = TestPublisher.createCold();
   private Flux<String> flux = coldPublisher.flux();

   @Test
   public void iterableDoesntCancelPublisher() {
      coldPublisher.next("a","b");

      prodCode();

      coldPublisher.assertNotCancelled();  //oups, memory resources not freed on the producer side
   }

   private void prodCode() {
      Iterable<String> iterable = flux.toIterable();

      System.out.println(iterable.iterator().next()); // prints "a"

      // no other element is interesting from the iterable
   }
}
