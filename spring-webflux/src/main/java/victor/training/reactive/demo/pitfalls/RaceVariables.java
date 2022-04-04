package victor.training.reactive.demo.pitfalls;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicInteger;

import static java.time.Duration.ofMillis;

public class RaceVariables {

   public class Repo {
      public Flux<String> findAll() {
         return Flux.interval(ofMillis(20)).take(10).map(i -> "User" + i);
      }
   }
   @RequiredArgsConstructor
   public static class SharingVariables {
      private final Repo repo;

      public Flux<String> problem() {
         AtomicInteger count = new AtomicInteger();
         return repo.findAll()
             .doOnNext(t -> count.incrementAndGet())
             .doOnComplete(() -> System.out.println("Count: " + count.get()));
      }
   }


   @Test
   public void doNotMutateLocalVariables() throws InterruptedException {
      SharingVariables sharing = new SharingVariables(new Repo());
      Flux<String> flux = sharing.problem();
      flux.subscribe();
      // TODO .subscribe again to the same flux

      Thread.sleep(300);
   }
}
