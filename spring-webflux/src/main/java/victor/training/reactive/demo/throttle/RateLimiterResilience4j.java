package victor.training.reactive.demo.throttle;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.internal.InMemoryRateLimiterRegistry;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
public class RateLimiterResilience4j {
   private final String url;
   private final RateLimiter rateLimiter;

   public RateLimiterResilience4j(String url, int rps) {
      this.url = url;
      // foarte simplu rate limiterul asta poate fi setat intr-un REDIS comun intre instante!!
      InMemoryRateLimiterRegistry rateLimiterRegistry = new InMemoryRateLimiterRegistry(RateLimiterConfig.ofDefaults());
      rateLimiter = rateLimiterRegistry.rateLimiter("test", RateLimiterConfig.custom()
          .limitRefreshPeriod(Duration.ofSeconds(1))
          .limitForPeriod(rps)
          .timeoutDuration(Duration.ofHours(1))
          .build());
   }

   public Mono<String> makeRequest(Number id) {
      return WebClient.create()
          .post()
          .uri(url)
          .accept(MediaType.ALL)
          .contentType(MediaType.TEXT_PLAIN)
          .syncBody("hello world")
          .retrieve()
          .bodyToMono(String.class)
          .doOnSubscribe(s -> log.info("Request allowed by rate limiter: " + id))
          .transform(RateLimiterOperator.of(rateLimiter)) // KEY PART
          .doOnNext(b -> log.info("Emit response for " + id));
   }
}