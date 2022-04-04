package victor.training.reactive.reactor.lite.solved;

import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.Unchecked;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import victor.training.reactive.reactor.lite.Part07Errors;
import victor.training.reactive.reactor.lite.domain.User;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import static java.util.Collections.emptyList;
import static reactor.core.scheduler.Schedulers.boundedElastic;

/**
 * @author Victor Rentea
 */
@Slf4j
public class Part07ErrorsSolved extends Part07Errors {
   @Override
   public Mono<User> betterCallSaulForBogusMono(Mono<User> mono) {
      return mono.onErrorReturn(User.SAUL);
   }

   @Override
   public Flux<User> betterCallSaulAndJesseForBogusFlux(Flux<User> flux) {
      return flux.onErrorResume(t -> Flux.just(User.SAUL, User.JESSE));
   }

   @Override
   public Flux<User> capitalizeMany(Flux<User> flux) {
      return flux.flatMap(u -> Mono.fromCallable(() -> capitalizeUser(u))); // trick = wrap exception
   }

   @Override
   public Mono<List<Order>> catchReturnDefault(List<Integer> ids) {
      return Flux.fromIterable(ids)
          .flatMap(id -> retrieveOrder(id))
          .collectList()
          .onErrorReturn(emptyList());
   }

   @Override
   public Mono<List<Order>> catchReturnBestEffort(List<Integer> ids) {
      return Flux.fromIterable(ids)
          .flatMap(id -> retrieveOrder(id)
              .onErrorResume(t -> Mono.empty()))
          .collectList();
   }

   @Override
   public Mono<List<Order>> catchAndStop(List<Integer> ids) {
      return Flux.fromIterable(ids)
          .concatMap(id -> retrieveOrder(id))
          .onErrorResume(e-> Flux.empty())
          .collectList();
   }

   @Override
   public Mono<List<Order>> catchRethrow(List<Integer> ids) {
      return Flux.fromIterable(ids)
          .flatMap(id -> retrieveOrder(id))
          .collectList()
          .onErrorMap(e -> new CustomException(e));
   }

   @Override
   public Mono<List<Order>> logRethrow(List<Integer> ids) {
      return Flux.fromIterable(ids)
          .flatMap(id -> retrieveOrder(id))
          .collectList()
          .doOnError(e -> log.error("BOOM", e));
   }

   @Override
   public Mono<List<Order>> recoverResumeAnotherMono(List<Integer> ids) {
      return Flux.fromIterable(ids)
          .flatMap(id -> retrieveOrder(id)
              .onErrorResume(t -> retrieveOrderBackup(id)))
          .collectList();
   }

   @Override
   public Mono<Void> tryFinally(List<Integer> ids) throws IOException {
      return Mono.using(
          // open file
          () -> new FileWriter("a.txt"),
          // use file - a bit blocking
          writer -> Flux.fromIterable(ids)
              .flatMap(this::retrieveOrder)
              .map(Order::toString)
              .map(s -> Unchecked.runnable(() -> writer.write(s)))
              .then(),
          // close file
          Unchecked.consumer(OutputStreamWriter::close))
          .subscribeOn(boundedElastic()); // because of file I/O

   }
}
