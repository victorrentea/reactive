package victor.training.reactive.usecase.debugging;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@SpringBootApplication
public class DebuggingApp {
   public static void main(String[] args) {
      SpringApplication.run(DebuggingApp.class, args);
   }

   private final SomeService someService;
   private final Apis apis;

   @GetMapping
   public Mono<Integer> catch_me_if_you_can() {
      return Mono.zip(
            apis.callGreen().map(someService::logic),
            apis.callBlue().map(someService::logic),
            Integer::sum);
   }
}
