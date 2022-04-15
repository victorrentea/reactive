package victor.training.reactive.usecase.debugging;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class Apis {

   public Mono<String> callGreen() {
      return Mono.just("1");
   }
   public Mono<String> callBlue() {
      return Mono.just("one");
   }
}
