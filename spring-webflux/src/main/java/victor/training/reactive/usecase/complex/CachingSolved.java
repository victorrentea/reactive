package victor.training.reactive.usecase.complex;

import reactor.core.publisher.Mono;

public class CachingSolved {

    //     1: al mano!
    private Mono<ProductRatingResponse> resolveRatingWithCache(Long productId) {
        return ExternalCacheClient.lookupInCache(productId)
                .switchIfEmpty(Mono.defer(() -> ExternalAPIs.getProductRating(productId))
                        .delayUntil(r -> ExternalCacheClient.putInCache(productId, r)));
    }

    // 2: @Cacheable+Mono.cache()
    //    @Autowired
    //    private RatingAdapter ratingAdapter;
    //    private Mono<ProductRatingResponse> resolveRatingWithCache(Long productId) {
    //        return ratingAdapter.resolveRating(productId);
    //    }
    //@Component
    //@Slf4j
    //public static class RatingAdapter {
    //
    //    @Cacheable("rating")
    //    public Mono<ProductRatingResponse> resolveRating(Long productId) {
    //        log.debug("entering resolveRating");
    //        return ExternalAPIs.getProductRating(productId).cache();
    //    }
    //}

    // 3: CacheManager + CacheMono (reactor-addons)
    //    @Autowired
    //    private CacheManager cacheManager;
    //    private Mono<ProductRatingResponse> resolveRatingWithCache(Long productId) {
    //        Cache ratingCache = cacheManager.getCache("rating");
    //        return CacheMono.lookup(k -> Mono.justOrEmpty(Optional.ofNullable( ratingCache.get(k)).map(valueWrapper -> (ProductRatingResponse)valueWrapper.get()))
    //                        .map(Signal::next),
    //                productId)
    //                .onCacheMissResume(() -> ExternalAPIs.getProductRating(productId))
    //                .andWriteWith((k, sig) -> Mono.fromRunnable(() ->
    //                        ratingCache.put(k, sig.get())));
    //    }

    //    @Autowired
    //        private CacheManager cacheManager;
    //    private Mono<ProductRatingResponse> resolveRatingWithCache(Long productId) {
    //        Cache cache = cacheManager.getCache("rating");
    //        ProductRatingResponse value = cache.get(productId, ProductRatingResponse.class);
    //        if (value != null) {
    //            return Mono.just(value);
    //        }
    //        return ExternalAPIs.getProductRating(productId)
    //                .doOnNext(r -> cache.put(productId, r));
    //    }

}
