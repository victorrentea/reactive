package victor.training.reactive.usecase.grouping;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Slf4j
public class Apis {

   public Mono<Void> apiA(Integer oddItem) {
      return api("A", oddItem);
   }
   public Mono<Void> apiB(Integer oddItem) {
      return api("B", oddItem);
   }

   public Mono<Void> apiC(List<Integer> evenItemPage) {
      return api("C", evenItemPage);
   }

   private Mono<Void> api(String apiName, Object data) {
      return Mono.delay(Duration.ofMillis(100))
          .doOnSubscribe(s -> log.info("Api" + apiName + ": {} START", data))
          .doOnNext(item -> log.info("Api" + apiName + ": {} END", data))
          .then();
   }
}
