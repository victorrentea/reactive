package victor.training.reactive.usecase.complex;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import static java.time.Duration.ofMillis;

@Slf4j
public class ExternalCacheClient { // faking a remote cache (involving network => Mono)
   public static Mono<ProductRatingResponse> lookupInCache(Long productId) {
      // if (true) return Mono.error(new RuntimeException("Sh*t happens..."));
      return Mono.fromSupplier(() -> {
         if (Math.random() < .5) {
            log.debug("Cache hit ✅: " + productId);
            return new ProductRatingResponse(5);
         } else {
            log.debug("Cache miss ❌: " + productId);
            return null;
         }
      }).delayElement(ofMillis(10));
   }

   public static Mono<Void> putInCache(Long productId, ProductRatingResponse rating) {
      // if (true) return Mono.error(new RuntimeException("Sh*t happens..."));
      log.info("Cache put ✍️: " + productId);
      return Mono.empty();
   }

}
