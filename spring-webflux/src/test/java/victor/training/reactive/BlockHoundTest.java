package victor.training.reactive;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;
import reactor.blockhound.BlockHound;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static victor.training.reactive.Utils.sleep;

@Slf4j
public class
BlockHoundTest {

   // unchangeable
   public String blockingApi(long id) {
      log.info("BEFORE " + id);
      sleep(100);
      log.info("AFTER");
      return "priceless stuff (matestecard)";
   }

   public Mono<String> nonBlockingCallToApi(long id) {
      return Mono.fromCallable(() -> blockingApi(id))
          .log("ABOVE")
          .subscribeOn(Schedulers.boundedElastic())
          .log("BELOW")
          ;
   }


   @Test
   void legacyBlockingIntegration() {
      System.out.println(Flux.interval(ofSeconds(1))
          .flatMap(id -> nonBlockingCallToApi(id))
          .take(3)
//                  .delayElements(ofMillis(300))
          .zipWith(Flux.defer(() -> Flux.interval(ofMillis(300))), (data, tick) -> data)
          .log("KILLING SPREE")
          .collectList()
          .block());

   }

   @Test
   void debug() {
//      Hooks.onOperatorDebug();
//      Hooks.resetOnOperatorDebug();

      List<Integer> list = Flux.just("1", "2", "a")
          .map(String::toUpperCase)
          .map(Integer::parseInt)
          .collectList()
          .checkpoint()
          .block();


   }

   @Test
   void restTemplateBlocks() {

      BlockHound.install();

      String s = Mono.fromSupplier(() -> {
               log.info("I am now in");
             return new RestTemplate().getForEntity("http://localhost:9999/api/product/6", String.class).getBody();
          })
          .subscribeOn(Schedulers.parallel())
          .block();
      System.out.println(s);
   }

   @Test
   void asyncRestTemplatDoeNOTBLOCK() {
      BlockHound.install();

      String s = Mono.defer(() -> {
               log.info("I am now in");
             CompletableFuture<ResponseEntity<String>> cf =
                 new AsyncRestTemplate().getForEntity("http://localhost:9999/api/product/6", String.class)
                 .completable();
             return Mono.fromFuture(cf);
          })
          .subscribeOn(Schedulers.parallel())
          .map(HttpEntity::getBody)
          .block();
      System.out.println(s);
   }

   @Test
   void test() {

      BlockHound.install();

//      Assertions.assertThatThrownBy(() ->
      Mono.delay(ofSeconds(1))
//                  .map(f->service.callStuff())
//                  .publishOn(Schedulers.boundedElastic()) // aka ThreadPool
//                  .publishOn()
          .doOnNext(it -> {
             try {
                Thread.sleep(10);
             } catch (InterruptedException e) {
                throw new RuntimeException(e);
             }
          })
          .block()
//   )
//          .hasCauseInstanceOf(BlockingOperationError.class)
      ;

   }

}
