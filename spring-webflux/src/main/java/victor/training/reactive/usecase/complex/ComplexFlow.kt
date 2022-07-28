package victor.training.reactive.usecase.complex

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import victor.training.reactive.Utils

@Component
class ComplexFlow(
    val webClient: WebClient
) {
    private val log = LoggerFactory.getLogger(ComplexFlowApp::class.java)

    fun mainFlow(productIds: List<Long>): Flux<Product> {
         return Flux.fromIterable(productIds)
            .buffer(2)
            .flatMapSequential(::retrieveMany, 10)

//            .flatMap { auditResealed(it).thenReturn(it) }
//            .sort(compareBy { it.id })

            .flatMapSequential { auditResealed(it) } // preserves order
    }


    private fun auditResealed(p: Product) =
        if (p.isResealed) {
            ExternalAPIs.auditResealedProduct(p).thenReturn(p)
        } else {
            Mono.just(p)
        }
//                .subscribe({ v: Void? -> Utils.noop(v) }) { error: Throwable? -> Utils.handleError(error) }

    private fun retrieveMany(productIds: List<Long>): Flux<Product> {
        log.info("Retrieve product IDs: $productIds")
        return webClient
            .post()
            .uri("http://localhost:9999/api/product/many")
            .bodyValue(productIds)
            .retrieve()
            .bodyToFlux(ProductDetailsResponse::class.java) // jackson parseaza progresiv JSONu cum vine si-ti emite semnale de date ProductDetails.
            .map { obj: ProductDetailsResponse -> obj.toEntity() }
    }


}