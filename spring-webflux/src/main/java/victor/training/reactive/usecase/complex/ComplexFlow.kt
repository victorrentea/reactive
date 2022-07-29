package victor.training.reactive.usecase.complex

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import victor.training.reactive.Utils
import victor.training.reactive.Utils.noop
import victor.training.reactive.Utils.sleep
import java.lang.RuntimeException
import java.time.Duration.ofMillis
import java.util.*

@Component
class ComplexFlow(
    val webClient: WebClient
) {
    private val log = LoggerFactory.getLogger(ComplexFlow::class.java)

    fun mainFlow(productIds: List<Long>): Flux<Product> {
// TODO victorrentea 2022-07-29: no variables of type MONO/FLUX
        val products =

            Flux.fromIterable(productIds)

                .buffer(2)
                .flatMap({ retrieveMany(it) },10)
                .collectList()

        val ratings=
            Flux.fromIterable(productIds)
                .flatMap { productId -> fillRatingWithCache(productId)
                    .map { rating -> productId to rating }
                    .onErrorResume { Mono.empty() } }
                .collectMap({it.first}, {it.second})

//        Mono.fromRunnable(gunoiCaTotJavaChemi())
        return products.zipWith(ratings) { p, r -> combine(p, r) }
            .flatMapMany { Flux.fromIterable(it) }
            .doOnNext { auditResealed(it) }
//            .log()
            .delayUntil {
                Mono.fromRunnable<Any> { gunoiCaTotJavaChemi() }
                    .subscribeOn(Schedulers.boundedElastic()) }
            .sort(compareBy{it.id})


//    .map { product.copy(rating = it) }
//    .onErrorResume { Mono.empty() } // sa nu ies cu ERROR de aici
//    .defaultIfEmpty(product) // nu pierd eleemntul

//        return Flux.fromIterable(productIds)
//            .buffer(2)
//            .flatMap({ retrieveMany(it) }, 10)
//            .flatMap { fillRatingWithCache(it) }
//            .doOnNext { auditResealed(it) }
//            .sort(compareBy{it.id})


    }
    fun gunoiCaTotJavaChemi() {
        log.debug("pe scheduler")
        if (true) throw RuntimeException()
        sleep(1000*60*10) //WSDL SOAP CALL
    }
    private fun combine(products: List<Product>, ratings: Map<Long, ProductRatingResponse>): List<Product> {
        return products.map { p -> p.copy(rating = ratings[p.id]) }
    }

//    @GetMapping
//    fun met():Mono<Void> {
//        rxRepo.save().subscribe() // GRESIT
//        return rxRepo.save()// CORRECT
//    }

    // daca switch if empty primeste next(rDinCache) atunci NU subscrie ci da mai jos la map un next(rDinCache)
    // daca switch if empty primeste empty() atunci subscrie la fluxul definit in EL
    //      (la delayUntil >>> subscribe >> getRating)
    //      cand rating emite next(rDinCall) > next(r) > delay (asteapta put)
    //      si emite mai jos catre map next(rPusInCache)
    private fun fillRatingWithCache(productId: Long): Mono<ProductRatingResponse> {
        return ExternalCacheClient.lookupInCache(productId)
            .timeout(ofMillis(100)) // cat las cache read
            .onErrorResume { Mono.empty() } // sa chem totusi realu daca cacheul e jos
            .switchIfEmpty( ExternalAPIs.getProductRating(productId)
                .doOnNext{ r -> ExternalCacheClient.putInCache(productId, r)
                    .timeout(ofMillis(100))
                    .subscribe({ noop() }, {Utils.handleError(it)})
                }
            )

    }


    private fun auditResealed(p: Product) {
        if (p.isResealed) {
            ExternalAPIs.auditResealedProduct(p)
                .subscribe({ noop(it) }, { Utils.handleError(it) })
        }
    }

    private fun retrieveMany(productIds: List<Long>): Flux<Product> {

        return webClient
            .post()
            .uri("http://localhost:9999/api/product/many")
            .bodyValue(productIds)
            .retrieve()
            .bodyToFlux(ProductDetailsResponse::class.java) // jackson parseaza progresiv JSONu cum vine si-ti emite semnale de date ProductDetails.
            .map { it.toEntity() }
            .doOnSubscribe{ log.info("Retrieve product IDs: $productIds") }
    }


}