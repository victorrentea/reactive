package victor.training.reactive.usecase.fluxconnect;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Objects;

import static victor.training.reactive.intro.Utils.waitForEnter;

@Slf4j
public class MultipleSubscribersFluxConnect {

   public static void main(String[] args) {
      Flux<String> hugeFlux = longRunningQuery();

      // TODO send items in pages of 10 to writePage(page) in parallel with announce(item)
      hugeFlux
          .buffer(10)
          .subscribe(MultipleSubscribersFluxConnect::writePage);

      hugeFlux
          .subscribe(MultipleSubscribersFluxConnect::announce);

      waitForEnter();
   }

   public static void writePage(List<String> page) {
      log.info("Writing {} items: {}", page.size(), page);
   }

   public static void announce(String item) {
      log.info("Announcing item {}", item);
   }

   private static Flux<String> longRunningQuery() {
      return Flux.defer(() -> {
         log.info("RUN the long expensive SELECT");
         return Flux.range(1, 100).map(Objects::toString);
      });
   }

}
