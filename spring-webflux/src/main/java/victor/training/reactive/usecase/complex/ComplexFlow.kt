package victor.training.reactive.usecase.complex

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import victor.training.reactive.Utils

@Component
class ComplexFlow(
    val webClient: WebClient
) {
    private val log = LoggerFactory.getLogger(ComplexFlowApp::class.java)

    fun mainFlow(productIds: List<Long>): Flux<Product> {
        return Flux.fromIterable(productIds)
            .buffer(2)
            .flatMap(::retrieveMany, 10)
            //              .delayUntil(ComplexFlowApp::auditResealed)
            .doOnNext(::auditResealed)
    }


    private fun auditResealed(p: Product) {
        if (p.isResealed) {
            ExternalAPIs.auditResealedProduct(p)
                .subscribe({ v: Void? -> Utils.noop(v) }) { error: Throwable? -> Utils.handleError(error) }
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
            .map { obj: ProductDetailsResponse -> obj.toEntity() }
    }


}