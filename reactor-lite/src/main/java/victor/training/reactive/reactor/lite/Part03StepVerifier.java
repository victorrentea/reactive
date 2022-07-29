/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package victor.training.reactive.reactor.lite;

import lombok.Data;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Timeout;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.PublisherProbe;
import victor.training.reactive.reactor.lite.domain.User;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static java.time.Duration.ofHours;
import static java.time.Duration.ofMillis;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class Part03StepVerifier {

    //========================================================================================

    //@Test
    // TODO Use StepVerifier to check that the flux parameter emits "foo" and "bar" elements then completes successfully.
    public void expectFooBarComplete(Flux<String> flux) {
        //      StepVerifier.create(flux)
        //              .expectNext("foo","bar")
        //              .verifyComplete(); // ca vine COMPLETION

        //      StepVerifier.create(flux)
        //              .assertNext(e -> assertThat(e.x).isEqualTo("foo"))
        //              .assertNext(e -> assertThat(e.x).isEqualTo("bar"))
        //              .verifyComplete(); // ca vine COMPLETION

        assertThat(flux.collectList().block())
                .containsExactly("foo", "bar");
    }

    //========================================================================================

    // TODO Use StepVerifier to check that the flux parameter emits
    //  "foo" and "bar" elements then a RuntimeException error.
    public void expectFooBarError(Flux<String> flux) {
        StepVerifier.create(flux)
                .expectNext("foo", "bar")
                .verifyErrorMatches(e -> e instanceof RuntimeException)
        ;
    }

    //========================================================================================

    // TODO Use StepVerifier to check that the flux parameter emits a User with "swhite" username
    // and another one with "jpinkman" then completes successfully.
    public void expectSkylerJesseComplete(Flux<User> flux) {
        //        StepVerifier.create(flux)
        //                .assertNext(e -> Assertions.assertThat(e.getUsername()).isEqualTo("swhite"))
        //                .assertNext(e -> Assertions.assertThat(e.getUsername()).isEqualTo("jpinkman"))
        //                .verifyComplete();
        //
        assertThat(flux.collectList().block())
                .extracting(User::getUsername)
                .containsExactly("swhite", "jpinkman");
    }

    //========================================================================================

    // TODO Expect 5 elements then complete and notice how long the test takes.
    public void expect5Elements(Flux<Long> flux) {
//flux.win
        StepVerifier.create(flux)
                .expectNextCount(5)
                .verifyComplete();
    }

    //========================================================================================

    // TODO Expect the value "later" to arrive 1 hour after subscribe(). Make the test complete in <1 second
    // Manipiulate virtual with StepVerifier#withVirtualTime/.thenAwait
    // TODO expect no sig nal for 30 minutes
    public void expectDelayedElement() {
        StepVerifier.withVirtualTime(() -> timeBoundFlow()
                        .timeout(ofHours(1).plus(ofMillis(500))))
                .expectSubscription()
                .expectNoEvent(Duration.ofMinutes(30))
                .thenAwait(Duration.ofHours(1))
                .expectNextCount(1)
                .verifyComplete();
    }

    public Mono<String> timeBoundFlow() {
        return Mono.just("later").delayElement(ofHours(1));
    }

    private void fail() {
        throw new AssertionError("workshop not implemented");
    }


    //========================================================================================

    // 🎖🌟🌟🌟🌟 WARNING HARD CORE 🌟🌟🌟🌟
    public void verifySubscribedOnce(Function<TestedProdClass, Mono<Void>> testedRxCode) {
        // given
        SomeRxRepo mockRepo = mock(SomeRxRepo.class);
        TestedProdClass testedObject = new TestedProdClass(mockRepo);
        PublisherProbe<Void> saveProbe = PublisherProbe.of(Mono.empty());
        when(mockRepo.save(any())).thenReturn(saveProbe.mono());

        // 1: create a PublisherProbe that tracks subscribe signals
        // 2. complete it
        // 3. program the repoMock to return the stubbed mono

        // when
        testedObject.correct().block();
        // TODO uncomment and make pass
        // testedRxCode.apply(testedObject).block();

        Assertions.assertThat(saveProbe.subscribeCount()).isEqualTo(1);
        // then
        // 4. assert the number of times the TestPublisher was subscribed to = 1
    }

    public interface SomeRxRepo {
        Mono<Void> save(User user);
    }

    //region tested production code
    @Data
    public static class TestedProdClass {
        private final SomeRxRepo repo;

        public Mono<Void> correct() { // <-- first test this
            repo.save(User.SKYLER); // asta e un apel de mock
            // care iti da un Mono care urmareste de cate ori i se face .subscrbibe
            return Mono.empty();
            //           return altApi.getById(id);

            //           return repo.save(User.SKYLER);
        }

        // try to test manually these or just use the
        public void noSubscribe() {
            repo.save(User.SKYLER);
        }

        public Mono<Void> doOnNext_noSubscribe() {
            return Mono.<Void>fromRunnable(() -> {
                        System.out.println("Pretend some remote work");
                    })
                    .doOnNext(x -> repo.save(User.SKYLER));
        }

        public Mono<Void> noDataSignal_noSubscribe() {
            return Mono.<Void>fromRunnable(() -> {
                        System.out.println("Pretend some remote work");
                    })
                    .flatMap(x -> repo.save(User.SKYLER));
        }

        public Mono<Void> correct_chained() {
            return Mono.<Void>fromRunnable(() -> {
                        System.out.println("Pretend some remote work");
                    })
                    .then(repo.save(User.SKYLER));
        }

        public Mono<Void> twice_resubscribe() {
            Mono<Void> save = repo.save(User.SKYLER);
            save.subscribe();
            return save;
        }
    }
    //endregion

}
