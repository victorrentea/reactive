package victor.training.reactive.usecase.complex

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class CacheSiCuMonoSiCuProxyuriDeSprijng {
    @Cacheable("iaoPasta") //piei
    fun method(productId: Long): Mono<ProductRatingResponse> {
        return ExternalAPIs.getProductRating(productId)
            .cache()
    }
}