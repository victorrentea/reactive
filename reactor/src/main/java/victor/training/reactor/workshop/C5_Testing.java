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

package victor.training.reactor.workshop;

import lombok.Data;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import victor.training.reactor.lite.domain.User;

import java.time.Duration;
import java.util.function.Function;


public class C5_Testing {
   private void fail() {
      throw new AssertionError("workshop not implemented");
   }

//========================================================================================

   // TODO Check that the flux parameter emits "foo" and "bar" elements then completes successfully.
   //  Option 1: use .block
   //  Option 2: use StepVerifier
   public void expectFooBarComplete(Flux<String> flux) {
      fail();
   }

//========================================================================================

   // TODO Use StepVerifier to check that the flux parameter emits "foo" and "bar" elements then a RuntimeException error.
   public void expectFooBarError(Flux<String> flux) {
      fail();
   }

//========================================================================================

   // TODO Check that the flux emits a User with "swhite" username and another one with "jpinkman" then completes successfully.
   public void expectSkylerJesseComplete(Flux<User> flux) {
      fail();
   }

//========================================================================================

   /** TODO make sure a side-effect function is subscribed exactly once */
   public void verifySubscribedOnce(Function<TestedProdClass, Mono<Void>> testedRxCode) {
      // === given
      SomeRxRepo mockRepo = Mockito.mock(SomeRxRepo.class);
      TestedProdClass testedObject = new TestedProdClass(mockRepo);

      // Mono<Void> saveMono = ... // create a completed empty mono
      // Mockito.when(mockRepo.save(User.SKYLER)). // tell repoMock to return that mono

      // === when - prod call
      testedObject.correct().block(); // Phase 1: warmup, understand code under test
      // testedRxCode.apply(testedObject).block(); // Phase 2: test an arbitrary function, to allow the tests to test your test !!

      // === then
      // TODO GOAL: check the .save() Mono was subscribed EXACTLY ONCE
   }

   public interface SomeRxRepo {
      Mono<Void> save(User user);
   }

   //region tested production code
   @Data
   public static class TestedProdClass {
      private final SomeRxRepo repo;

      public Mono<Void> correct() { // <-- first test this
         return repo.save(User.SKYLER);
      }
      // try to test manually these or just use the
      public void noSubscribe() {
         // BUG🐞: forgot to subscribe
         repo.save(User.SKYLER);
      }

      public Mono<Void> doOnNext_noSubscribe() {
         // BUG🐞: forgot to subscribe
         return Mono.<Void>fromRunnable(() -> {
                System.out.println("Pretend some remote work");
             })
             .doOnNext(x -> repo.save(User.SKYLER));
      }
      public Mono<Void> noDataSignal_noSubscribe() {
         // BUG🐞: flatMap does not execute the -> because there is no data signal from above
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


   //========================================================================================
   // TODO Expect the value "later" to arrive 1 hour after subscribe(). Make the test complete in <1 second
   //    Manipulate virtual with StepVerifier#withVirtualTime(->) and #thenAwait
   // TODO expect no signal for 30 minutes

   public void expectDelayedElement() {
      StepVerifier.create(timeBoundFlow())
      // TODO
      ;
   }

   public Mono<String> timeBoundFlow() {
      return Mono.just("later").delayElement(Duration.ofHours(1));
   }



}
