/*
 * Copyright (c) 2011-2017 Pivotal Software Inc, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package victor.training.reactive.reactor.lite;

import lombok.extern.slf4j.Slf4j;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.blockhound.BlockHound;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import victor.training.reactive.intro.Utils;
import victor.training.reactive.reactor.lite.Part07Errors.CustomException;
import victor.training.reactive.reactor.lite.Part07Errors.Order;
import victor.training.reactive.reactor.lite.domain.User;
import victor.training.reactive.reactor.lite.solved.Part07ErrorsSolved;
import victor.training.util.CaptureSystemOutput;
import victor.training.util.NonBlocking;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static reactor.core.scheduler.Schedulers.parallel;

@Slf4j
public class Part07ErrorsTest {

   Part07Errors workshop = new Part07Errors();
//	Part07Errors workshop = new Part07ErrorsSolved();

//========================================================================================

   @Test
   public void monoWithValueInsteadOfError() {
      // ERROR
      Mono<User> mono = workshop.betterCallSaulForBogusMono(Mono.error(new IllegalStateException()));
      StepVerifier.create(mono)
          .expectNext(User.SAUL)
          .verifyComplete();

      // OK
      mono = workshop.betterCallSaulForBogusMono(Mono.just(User.SKYLER));
      StepVerifier.create(mono)
          .expectNext(User.SKYLER)
          .verifyComplete();
   }

//========================================================================================

   @Test
   public void fluxWithValueInsteadOfError() {
      // ERROR
      Flux<User> flux = workshop.betterCallSaulAndJesseForBogusFlux(Flux.error(new IllegalStateException()));
      StepVerifier.create(flux)
          .expectNext(User.SAUL, User.JESSE)
          .verifyComplete();

      // OK
      flux = workshop.betterCallSaulAndJesseForBogusFlux(Flux.just(User.SKYLER, User.WALTER));
      StepVerifier.create(flux)
          .expectNext(User.SKYLER, User.WALTER)
          .verifyComplete();
   }

//========================================================================================

   @Test
   public void capitalizeMany() {
      Flux<User> flux = workshop.capitalizeMany(Flux.just(User.SAUL, User.JESSE));

      StepVerifier.create(flux)
          .verifyError(Part07Errors.GetOutOfHereException.class);
   }

//========================================================================================

   @Test
   @NonBlocking
   public void catchReturnDefault() {
      // ERROR
      StepVerifier.create(workshop.catchReturnDefault(asList(1, 2, -1, 4)))
          .expectNextMatches(productsWithIds())
          .verifyComplete();

      //OK
      StepVerifier.create(workshop.catchReturnDefault(asList(1, 2)))
          .expectNextMatches(productsWithIds(1, 2))
          .verifyComplete();
   }

   //========================================================================================
   @Test
   @NonBlocking
   public void catchReturnBestEffort() {
      // OK
      StepVerifier.create(workshop.catchReturnBestEffort(List.of(1, 2)))
          .expectNextMatches(productsWithIds(1, 2))
          .verifyComplete();

      // ERROR
      StepVerifier.create(workshop.catchReturnBestEffort(List.of(1, 2, -1, 4)))
          .expectNextMatches(productsWithIds(1, 2, 4))
          .verifyComplete();
   }

   //========================================================================================
   @Test
   @NonBlocking
   public void catchAndStop() {
      // OK
      StepVerifier.create(workshop.catchAndStop(List.of(1, 2)))
          .expectNextMatches(productsWithIds(1, 2))
          .verifyComplete();

      // ERROR
      StepVerifier.create(workshop.catchAndStop(List.of(1, 2, -1, 4)))
          .expectNextMatches(productsWithIds(1, 2))
          .verifyComplete();
   }

   //========================================================================================
   @Test
   @NonBlocking
   public void catchRethrow() {
      // OK
      StepVerifier.create(workshop.catchRethrow(List.of(1, 2)))
          .expectNextMatches(productsWithIds(1, 2))
          .verifyComplete();

      // ERROR
      StepVerifier.create(workshop.catchRethrow(List.of(1, 2, -1, 4)))
          .expectErrorMatches(e -> e instanceof  CustomException && e.getCause() instanceof RuntimeException)
          .verify();
   }


   //========================================================================================
   @Test
   @NonBlocking
   @CaptureSystemOutput
   public void logRethrow(CaptureSystemOutput.OutputCapture outputCapture) {
      outputCapture.expect(CoreMatchers.containsString("BOOM"));

      StepVerifier.create(workshop.logRethrow(List.of(1, 2, -1, 4)))
          .expectErrorMatches(e -> e instanceof IllegalArgumentException)
          .verify();
   }

   //========================================================================================
   @Test
   @NonBlocking
   public void recoverResumeAnotherMono() {

      StepVerifier.create(workshop.recoverResumeAnotherMono(List.of(1, -1, 4)))
          .expectNextMatches(list -> {
               assertThat(list)
                   .contains(new Order(-1).backup())
                   .extracting("id").containsExactlyInAnyOrder(1, -1, 4);
               return true;
          })
          .verifyComplete();


   }

   //========================================================================================
   @Test
   public void tryFinally() throws IOException {
      StepVerifier.create(workshop.tryFinally(asList(1, 4)))
          .verifyComplete();
   }


   private Predicate<List<Order>> productsWithIds(Integer... expectedIds) {
      return list -> {
         Set<Integer> actualIds = list.stream().map(Order::getId).collect(toSet());
         if (actualIds.equals(Set.of(expectedIds))) {
            return true;
         } else {
            System.out.println("Actual order ids : " + actualIds + " didn't matched the expected ids: " + Arrays.toString(expectedIds));
            return false;
         }
      };
   }


}
