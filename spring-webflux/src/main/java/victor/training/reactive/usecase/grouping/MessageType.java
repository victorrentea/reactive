package victor.training.reactive.usecase.grouping;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;

enum MessageType {
   TYPE1_NEGATIVE(GroupingFluxes::handleType1),
   TYPE2_ODD(GroupingFluxes::handleType2),
   TYPE3_EVEN(GroupingFluxes::handleType3);

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
