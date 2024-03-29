package victor.training.reactive.usecase.complex;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
class ExternalAPIs {

   public static Mono<Void> auditResealedProduct(Product product) {
      // if (true) return Mono.error(new RuntimeException("Sh*t happens..."));
      return WebClient.create().get().uri("http://localhost:9999/api/audit-resealed/" + product)
              .retrieve()
              .toBodilessEntity()
              .doOnSubscribe(s -> log.info("Calling Audit REST: " +product.getId()))
              .then();
   }

   public static Mono<ProductRatingResponse> getProductRating(long productId) {
      // if (true) return Mono.error(new RuntimeException("Sh*t happens..."));

      return WebClient.create().get().uri("http://localhost:9999/api/rating/{}", productId)
              .retrieve()
              .bodyToMono(ProductRatingResponse.class)
              .doOnSubscribe(s -> log.info("Calling getProductRating API: " + productId))
              ;
   }


}
