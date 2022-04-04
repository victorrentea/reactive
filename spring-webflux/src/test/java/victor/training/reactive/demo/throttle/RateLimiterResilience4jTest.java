package victor.training.reactive.demo.throttle;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import victor.training.util.WireMockExtension;

import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class RateLimiterResilience4jTest {

    public static final int MAX_RPS = 3;
    @RegisterExtension
    public WireMockExtension wireMock = new WireMockExtension(9999);
    private RateLimiterResilience4j target = new RateLimiterResilience4j(
        "http://localhost:9999/testing", MAX_RPS);

    @Test
    public void whyDoesntThisWorkQuestionMarkQuestionMark() {
        // Given

        wireMock.addStubMapping(stubFor(post(urlPathEqualTo("/testing"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withBody("Hello!")
                    .withFixedDelay(1000))));

        // When
        int NUMBER_OF_REQUESTS = 30;
        Flux<String> flux =
            Flux.interval(Duration.ofMillis(1))
                .doOnNext(e -> log.info("Incoming request " + e))
                .take(NUMBER_OF_REQUESTS)
                .onBackpressureBuffer()
                //Flux.range(0, NUMBER_OF_REQUESTS).parallel().runOn(Schedulers.boundedElastic())
                .flatMap(id -> target.makeRequest(id));

        long startTime = System.currentTimeMillis();
        StepVerifier.create(flux).expectNextCount(NUMBER_OF_REQUESTS).verifyComplete();
        long endTime = System.currentTimeMillis();

        log.info("Took {} ", endTime - startTime);

        assertThat(endTime - startTime)
                .describedAs("I don't understand why this is not taking longer than this")
                .isGreaterThanOrEqualTo((NUMBER_OF_REQUESTS / MAX_RPS) * 1000);
    }
}