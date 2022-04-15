package victor.training.reactive.usecase.migrating;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@SpringBootApplication
public class MigrationApp {
   public static void main(String[] args) {
//      Utils.installBlockHound();
      SpringApplication.run(MigrationApp.class, args);
   }

   @GetMapping
   public Mono<String> reactiveEndpoint(@RequestParam(defaultValue = "1") String id) {
      return WebClient.create().get().uri("localhost:8080/call").retrieve()
          .bodyToMono(String.class) // on netty scheduler
          // .delayElement(Duration.ofMillis(1)) // moves on parallel
          .doOnNext(response -> SomeLibrary.validate(response))
          .map(String::toUpperCase);
   }

   @GetMapping
   public Mono<String> call() {
      return Mono.just("data");
   }
}
