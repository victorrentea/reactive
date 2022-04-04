package victor.training.reactive.usecase.monitoringinfinite;

import reactor.core.publisher.Mono;

class AuditApi {
   static Mono<Void> auditOrderNotFound(Long id) {
      return Mono.fromRunnable(() -> {
         System.out.println("CALL AUDIT " + id);
         if (Math.random() < .1) {
            throw new IllegalArgumentException("Random bum");
         }
      });
   }
}