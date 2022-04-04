package victor.training.reactive.reactor.complex;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import static java.time.Duration.ofMillis;

@Slf4j
public class ExternalCacheClient {
   public static Mono<ProductRatingResponse> lookupInCache(Long productId) {
      return Mono.defer(() -> {
         if (Math.random() < .5) {
            log.debug("Cache hit");
            return Mono.just(new ProductRatingResponse(5));
         } else {
            return Mono.empty();
         }
      }).delayElement(ofMillis(10));//.publishOn(Schedulers.single());
   }

   public static Mono<Void> putInCache(Long productId, ProductRatingResponse rating) {
      log.info("Put in cache " + productId);
      if (true) {
         throw new RuntimeException("buba");
      }
      return Mono.empty();//.delayElement(ofMillis(10)).then();
   }
}
