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

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import victor.training.reactive.intro.Utils;
import victor.training.reactive.reactor.lite.domain.User;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static java.time.Duration.ofMillis;
import static java.util.Collections.emptyList;
import static reactor.core.scheduler.Schedulers.boundedElastic;

public class Part07Errors {
   private static final Logger log = LoggerFactory.getLogger(Part07Errors.class);
//========================================================================================

   // TODO Return a Mono<User> containing User.SAUL when an error occurs in the input Mono, else do not change the input Mono.
   public Mono<User> betterCallSaulForBogusMono(Mono<User> mono) {
      return null;
   }

//========================================================================================

   // TODO Return a Flux<User> containing User.SAUL and User.JESSE when an error occurs in the input Flux, else do not change the input Flux.
   public Flux<User> betterCallSaulAndJesseForBogusFlux(Flux<User> flux) {
      return null;
   }

//========================================================================================

   // TODO Implement a method that capitalizes each user of the incoming flux using the
   // #capitalizeUser method and emits an error containing a GetOutOfHereException error
   public Flux<User> capitalizeMany(Flux<User> flux) {
      return null;
   }

   protected User capitalizeUser(User user) throws GetOutOfHereException {
      if (user.equals(User.SAUL)) {
         throw new GetOutOfHereException();
      }
      return new User(user.getUsername(), user.getFirstname(), user.getLastname());
   }

   protected static final class GetOutOfHereException extends Exception {
      private static final long serialVersionUID = 0L;
   }
//========================================================================================
   // TODO retrieve all Orders with retrieveOrder. In case **ANY** fails, return an empty list.
   public Mono<List<Order>> catchReturnDefault(List<Integer> ids) {
      // TODO: Convert this imperative blocking code to reactive
      try {
         List<Order> orders = new ArrayList<>();
         for (Integer id : ids) {
            orders.add(retrieveOrder(id).block());// TODO REMOVE blocking
         }
         return Mono.just(orders);
      } catch (Exception e) {
         return Mono.just(emptyList());
      }
   }

   //========================================================================================
   // TODO return those items that were retrieve successfully
   public Mono<List<Order>> catchReturnBestEffort(List<Integer> ids) {
      // TODO: Convert this imperative blocking code to reactive
      List<Order> orders = new ArrayList<>();
      for (Integer id : ids) {
         try {
            Order order = retrieveOrder(id).block();// TODO REMOVE blocking
            orders.add(order);
         } catch (IllegalArgumentException e) {
            // ignore
         }
      }
      return Mono.just(orders);
   }

   //========================================================================================
   // TODO return the items that were retrieve, never request further items (==> no async prefetch)
   public Mono<List<Order>> catchAndStop(List<Integer> ids) {
      // TODO: Convert this imperative blocking code to reactive : tip: use concatMap as the traveral needs to be sequential
      List<Order> orders = new ArrayList<>();
      try {
         for (Integer id : ids) {
            Order order = retrieveOrder(id).block();// TODO REMOVE blocking
            orders.add(order);
         }
      } catch (IllegalArgumentException e) {
      }
      return Mono.just(orders);
   }

   //========================================================================================
   // TODO fail at first error, rethrowing the exception wrapped in a CustomException
   public Mono<List<Order>> catchRethrow(List<Integer> ids) {
      // TODO: Convert this imperative blocking code to reactive
      try {
         List<Order> orders = new ArrayList<>();
         for (Integer id : ids) {
            Order order = retrieveOrder(id).block();
            orders.add(order);
         }
         return Mono.just(orders);
      } catch (IllegalArgumentException e) {
         throw new CustomException(e);
      }
   }

   //========================================================================================
   // TODO fail at any error, log the error and rethrow it
   public Mono<List<Order>> logRethrow(List<Integer> ids) {
      // TODO: Convert this imperative blocking code to reactive
      try {
         List<Order> orders = new ArrayList<>();
         for (Integer id : ids) {
            Order order = retrieveOrder(id).block();
            orders.add(order);
         }
         return Mono.just(orders);
      } catch (IllegalArgumentException e) {
         log.error("BOOM", e);
         throw e;
      }
   }

   //========================================================================================
   // TODO for any item, if an error occurs fall back by calling retrieveOrderBackup (blocking)
   public Mono<List<Order>> recoverResumeAnotherMono(List<Integer> ids) {
      // TODO: Convert this imperative blocking code to reactive
      List<Order> orders = new ArrayList<>();
      for (Integer id : ids) {
         Order order;
         try {
            order = retrieveOrder(id).block(); // TODO remove block
         } catch (Exception e) {
            order = retrieveOrderBackup(id).block(); // TODO remove block
         }
         orders.add(order);
      }
      return Mono.just(orders);
   }

   //========================================================================================
   // TODO  close the writer at the end signal (error or completion)
   // TODO [pro] what if the subscriber retries? Tip: Mono.fromSupplier
   public Mono<Void> tryFinally(List<Integer> ids) throws IOException {
      try(FileWriter writer = new FileWriter("a.txt")) {
         for (Integer id : ids) {
            Order order = retrieveOrder(id).block();
            writer.write(order.toString());
         }
      }
      return Mono.empty();
   }


   public static class CustomException extends RuntimeException {
      public CustomException(Throwable cause) {
         super(cause);
      }
   }

   protected Mono<Order> retrieveOrder(int id) { // imagine a network call
      return Mono.fromCallable(() -> {
         if (id < 0) {
            throw new IllegalArgumentException("intentional");
         } else {
            return new Order(id);
         }
      }).subscribeOn(boundedElastic()); // moving the mono to another scheduler makes .block() on it offend BlockHound
   }
   protected Mono<Order> retrieveOrderBackup(int id) { // imagine a local fast storage (~cache)
      return Mono.just(new Order(id).backup()).subscribeOn(boundedElastic());
   }

   @Data
   public static class Order {
      private final Integer id;
      private boolean backup; // mutable data in multithreaded context, God help us all !

      public Order backup() {
         this.backup = true;
         return this;
      }

      public boolean isBackup() {
         return backup;
      }
   }



}
