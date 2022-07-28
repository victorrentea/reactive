package victor.training.reactive.usecase.complex;

import kotlin.random.Random;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import victor.training.reactive.Utils;

import java.time.Duration;

@Slf4j
class ExternalAPIs {

    public static Mono<Void> auditResealedProduct(Product product) {
        // if (true) return Mono.error(new RuntimeException("Sh*t happens..."));
        return WebClient.create().get().uri("http://localhost:9999/api/audit-resealed/" + product)
                .retrieve()
                .toBodilessEntity()
                .doOnSubscribe(s -> log.info("Calling Audit REST: " + product.getId()))
                .then()
//                .delaySubscription(Duration.ofMillis(Random.Default.nextLong(100)))
                ;
    }

    public static Mono<ProductRatingResponse> getProductRating(Long productId) {
//        if (productId == null) {
//            throw new IllegalArgumentException();
//        }
//        System.out.printf("ASTA RULEAZA PT FIECARE ELEMENT!");

        // codul asta ruelaza pentru TOATE produsele, chiar daca le-ai gasit in cache.!!!!
//        apelSoapBlocant1sec() // integrari cu drivere/resurse BLOCANTE WSDL
        // RMI CORBA
        // log

        log.debug("PROST/MINCINOS: Now calling get product rating " + productId);
        // if (true) return Mono.error(new RuntimeException("Sh*t happens..."));

        return WebClient.create().get().uri("http://localhost:9999/api/rating/{}", productId)
                .retrieve()
                .bodyToMono(ProductRatingResponse.class)
                .doOnSubscribe(s -> log.info("BUN: Calling getProductRating API: " + productId))
                ;
    }


}
