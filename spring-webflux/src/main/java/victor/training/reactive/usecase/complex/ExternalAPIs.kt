package victor.training.reactive.usecase.complex

import org.reactivestreams.Subscription
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

 object ExternalAPIs {
    private val log = LoggerFactory.getLogger(ExternalAPIs::class.java)
    fun auditResealedProduct(product: Product): Mono<Void> {
        // if (true) return Mono.error(new RuntimeException("Sh*t happens..."));
        return WebClient.create().get().uri("http://localhost:9999/api/audit-resealed/$product")
            .retrieve()
            .toBodilessEntity()
            .doOnSubscribe { s: Subscription? -> log.info("Calling Audit REST: " + product.id) }
            .then() //                .delaySubscription(Duration.ofMillis(Random.Default.nextLong(100)))
    }

    fun getProductRating(productId: Long): Mono<ProductRatingResponse> {
        return if (Math.random() < .5) {
            Mono.error(IllegalArgumentException("INTENTIONAT"))
        } else WebClient.create().get().uri("http://localhost:9999/api/rating/{}", productId)
            .retrieve()
            .bodyToMono(ProductRatingResponse::class.java)
            .doOnSubscribe { s: Subscription? -> log.info("BUN: Calling getProductRating API: $productId") }
        //        log.debug("PROST/MINCINOS: Now calling get product rating " + productId);
    }
}