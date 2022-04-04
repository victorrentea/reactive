package victor.training.reactive.usecase.sinks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static java.time.Duration.ofMillis;
import static java.util.stream.Collectors.toMap;

@Slf4j
@Component
class Apis {
   private static final AtomicInteger counter = new AtomicInteger(0);

   public Mono<Map<String, PlusCode>> getPlusCode(List<String> addresses) {
      Map<String, PlusCode> map = addresses.stream().collect(toMap(
          Function.identity(),
          e -> new PlusCode("CODE" + counter.incrementAndGet())));
      return Mono.delay(ofMillis(100)).thenReturn(map)
          .doOnSubscribe(e -> log.info("Calling getPlusCode(n={})", addresses.size()));
   }
}
