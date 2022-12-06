package victor.training.reactive.usecase.complex;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import victor.training.reactive.Utils;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
public class ComplexFlowSolved {

    //   @Timed
    public Mono<List<Product>> mainFlow(List<Long> productIds) {
        return Flux.fromIterable(productIds)
                .buffer(2)
                .name("mainFlow")
                .metrics()
                .doOnNext(x -> log.info("Deasupra call retea"))
                .flatMap(ComplexFlowSolved::retrieveMultipleProducts, 4)
                .delayElements(Duration.ofMillis(1))
                .doOnNext(x -> {
                    Utils.sleep(100);
                    log.info("Sub call retea");
                })

                .doOnNext(ComplexFlowSolved::auditProduct)
                .flatMap(this::enhanceWithRating)
                .collectList()

                .doOnNext(e -> log.info("Inainte " + e))
                .delayElement(Duration.ofMillis(100))
                .doOnNext(e -> log.info("Dupa " + e))
                ;
    }

    private static void auditProduct(Product product) {
        if (product.isResealed()) {
            ExternalAPIs.auditResealedProduct(product).subscribe(v -> {}, e -> log.error("OMG i'm afraid", e));
        }
    }

    private Mono<Product> enhanceWithRating(Product product) {
        return ExternalCacheClient.lookupInCache(product.getId())
                .doOnError(Throwable::printStackTrace)
                .onErrorResume(t->Mono.empty())
                .switchIfEmpty(
                        Mono.defer(() -> ExternalAPIs.getProductRating(product.getId()))
                                .onErrorResume(t -> Mono.empty())

                                .doOnNext(rating ->ExternalCacheClient.putInCache(product.getId(), rating)
                                        .doOnError(t -> log.trace("Boom " + t))
                                        .subscribe())
                )
                .map(product::withRating)
                .defaultIfEmpty(product);
    }
    private static Flux<Product> retrieveMultipleProducts(List<Long> productIdList) {
        if (true) Flux.error(new RuntimeException());

        return WebClient.create()
                .post()
                .uri("http://localhost:9999/api/product/many")
                .bodyValue(productIdList)
                .retrieve()
                .bodyToFlux(ProductDetailsResponse.class)
//          .samplTi
                .name("LoadProducts")
                .metrics()
                .map(ProductDetailsResponse::toEntity)
                ;


    }

//   private static Mono<Product> retrieveProduct(Long productId) {
//      return WebClient.create()
//          .get()
//          .uri("http://localhost:9999/api/product/" + productId)
//          .retrieve()
//          .bodyToMono(ProductDetailsResponse.class)
//          .map(ProductDetailsResponse::toEntity);
//   }
}
