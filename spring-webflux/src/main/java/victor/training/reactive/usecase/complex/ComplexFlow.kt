package victor.training.reactive.usecase.complex

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import victor.training.reactive.Utils
import victor.training.reactive.Utils.noop

@Component
class ComplexFlow(
    val webClient: WebClient
) {
    private val log = LoggerFactory.getLogger(ComplexFlowApp::class.java)

    fun mainFlow(productIds: List<Long>): Flux<Product> {
        return Flux.fromIterable(productIds)
            .buffer(2)
            .flatMap({ retrieveMany(it) }, 10)
            .doOnNext { auditResealed(it) }
            .flatMap { fillRating(it) }


    }

    private fun fillRating(product: Product): Mono<Product> =
        ExternalAPIs.getProductRating(product.id)
            .map { product.withRating(it) } // cand nu poti face clasa sa fie dataclass


    private fun auditResealed(p: Product) {
        if (p.isResealed) {
            ExternalAPIs.auditResealedProduct(p)
                .subscribe({ noop(it) }, { Utils.handleError(it) })
        }
    }

    private fun retrieveMany(productIds: List<Long>): Flux<Product> {
        log.info("Retrieve product IDs: $productIds")
        return webClient
            .post()
            .uri("http://localhost:9999/api/product/many")
            .bodyValue(productIds)
            .retrieve()
            .bodyToFlux(ProductDetailsResponse::class.java) // jackson parseaza progresiv JSONu cum vine si-ti emite semnale de date ProductDetails.
            .map { it.toEntity() }
    }


}