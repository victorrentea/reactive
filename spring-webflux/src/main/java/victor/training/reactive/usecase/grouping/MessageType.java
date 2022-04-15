package victor.training.reactive.usecase.grouping;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;

import static java.time.Duration.ofMillis;

enum MessageType {
   TYPE1_NEGATIVE((groupingFluxes, groupedFlux) -> Mono.empty()),
   TYPE2_ODD((groupingFluxes, groupedFlux) -> groupedFlux
       .flatMap(m2 ->Mono.zip(groupingFluxes.apis.apiA(m2), groupingFluxes.apis.apiB(m2)))
       .then()),
   TYPE3_EVEN((groupingFluxes, groupedFlux) -> groupedFlux
       .bufferTimeout(3, ofMillis(500))
       .flatMap(pageOfType3 -> groupingFluxes.apis.apiC(pageOfType3))
       .then());

   public final BiFunction<GroupingFluxes, Flux<Integer>, Mono<Void>> handleFunction;

   MessageType(BiFunction<GroupingFluxes, Flux<Integer>, Mono<Void>> handleFunction) {
      this.handleFunction = handleFunction;
   }

   public static MessageType forMessage(Integer message) {
      if (message < 0) return TYPE1_NEGATIVE;
      if (message % 2 == 1) return TYPE2_ODD;
      else return TYPE3_EVEN;
   }
}
