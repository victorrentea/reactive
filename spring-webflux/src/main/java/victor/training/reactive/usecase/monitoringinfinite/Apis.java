package victor.training.reactive.usecase.monitoringinfinite;

import reactor.core.publisher.Mono;

public class Apis {
   public Mono<Void> auditOrderNotFound(Long id) {
      return Mono.fromRunnable(() -> {
         System.out.println("CALL AUDIT " + id);
         if (Math.random() < .1) {
            throw new IllegalArgumentException("Random bum");
         }
      });
   }
   public Mono<Boolean> isOrderPresent(Long id) {
      return Mono.deferContextual(contextView -> {
         System.out.println("Current user is " + contextView.get("username"));
         return Mono.fromSupplier(() -> Math.random() < .5);
      });
   }
}
