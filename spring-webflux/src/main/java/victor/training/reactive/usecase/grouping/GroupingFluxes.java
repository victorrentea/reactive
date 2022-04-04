package victor.training.reactive.usecase.grouping;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import victor.training.reactive.intro.Utils;

import java.util.List;

@Slf4j
public class GroupingFluxes {
   public static void main(String[] args) {
      // TODO send odd numbers to sendOdd(), while even numbers to sendEven(), respectively
      // TODO send even numbers in pages of 10 items
      // TODO for odd numbers call preSendOdd() before sendOdd()
      // TODO negative items should not trigger any sending

      Flux<Integer> kafkaStream = Flux.range(0, 100);
      kafkaStream
          .flatMap(item -> {
             if (getType(item) == NumberType.ODD) return sendOdd(item);
             else return sendEven(item);})
          .subscribe();

      Utils.waitForEnter();
   }

   public static NumberType getType(int number) {
      return number % 2 == 1 ? NumberType.ODD : NumberType.EVEN;
   }

   public static Mono<Void> sendEven(Integer item) {
      return Mono.fromRunnable(() -> log.info("Sending even numbers: {}", item));
   }


   public static Mono<Void> sendEvenInPages(List<Integer> itemsPage) {
      return Mono.fromRunnable(() -> log.info("Sending even numbers page: {}", itemsPage));
   }
   public static Mono<Void> preSendOdd(Integer item) {
      return Mono.fromRunnable(() -> log.info("pre-send odd number: {}", item));
   }

   public static Mono<Void> sendOdd(Integer item) {
      return Mono.fromRunnable(() -> log.info("Sending odd numbers: {}", item));
   }
}

enum NumberType {
   ODD, EVEN
   // NEGATIVE -> nothing to do
;
   static NumberType forNumber(Integer i) {
//      if (i<0) return NEGATIVE;
      if (i % 2 == 0) {
         return EVEN;
      } else {
         return ODD;
      }
   }
}