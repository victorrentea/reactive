package victor.training.reactive.usecase.monitoringinfinite;

import reactor.core.publisher.Mono;

class OrderApi {
   static Mono<Boolean> isOrderPresent(Long id) {
      return Mono.deferContextual(contextView -> {
         System.out.println("Current user is " + contextView.get("username"));
         return Mono.fromSupplier(() -> Math.random() < .5);
      });
   }
}