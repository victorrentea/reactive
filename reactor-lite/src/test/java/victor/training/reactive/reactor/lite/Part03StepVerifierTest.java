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

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import victor.training.reactive.reactor.lite.Part03StepVerifier.TestedProdClass;
import victor.training.reactive.reactor.lite.domain.User;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class Part03StepVerifierTest {

   Part03StepVerifier workshop = new Part03StepVerifier();
//   Part03StepVerifier workshop = new Part03StepVerifierSolved();

//========================================================================================

   @Test
   public void expectFooBarComplete() {
      workshop.expectFooBarComplete(Flux.just("foo", "bar"));
   }

//========================================================================================

   @Test
   public void expectFooBarError() {
      workshop.expectFooBarError(Flux.just("foo", "bar").concatWith(Mono.error(new RuntimeException())));
   }

//========================================================================================

   @Test
   public void expectSkylerJesseComplete() {
      workshop.expectSkylerJesseComplete(Flux.just(new User("swhite", null, null), new User("jpinkman", null, null)));
   }

//========================================================================================

   @Test
   public void expect5Elements() {
      workshop.expect5Elements(Flux.interval(Duration.ofSeconds(1)).take(5));
   }

//========================================================================================

   @Test
   public void expectDelayedElement() {
      workshop.expectDelayedElement();
   }

   //========================================================================================
   @Test
   public void verifySubscribedOnce_ok() {
      workshop.verifySubscribedOnce(TestedProdClass::correct);
   }

   @Test
   public void verifySubscribedOnceFails_failsNoSubscribe() {
      assertThatThrownBy(() ->
          workshop.verifySubscribedOnce(testedProdClass -> {
             testedProdClass.noSubscribe();
             return Mono.empty();
          }))
          .isInstanceOf(AssertionFailedError.class)
          .hasMessageContaining("<0L>");
   }

   @Test
   public void verifySubscribedOnceFails_doOnNext_noSubscribe() {
      assertThatThrownBy(() ->
          workshop.verifySubscribedOnce(TestedProdClass::doOnNext_noSubscribe))
          .isInstanceOf(AssertionFailedError.class)
          .hasMessageContaining("<0L>");
   }

   @Test
   public void verifySubscribedOnceFails_noDataSignal_noSubscribe() {
      assertThatThrownBy(() ->
          workshop.verifySubscribedOnce(TestedProdClass::noDataSignal_noSubscribe))
          .isInstanceOf(AssertionFailedError.class)
          .hasMessageContaining("<0L>");
   }

   @Test
   public void verifySubscribedOnceOK_correct_chained() {
      workshop.verifySubscribedOnce(TestedProdClass::correct_chained);
   }

   @Test
   public void verifySubscribedOnceFails_twice_resubscribe() {
      assertThatThrownBy(() ->
          workshop.verifySubscribedOnce(TestedProdClass::twice_resubscribe))
          .isInstanceOf(AssertionFailedError.class)
          .hasMessageContaining("<2L>");
   }
}


