package victor.training.reactive.usecase.monitoringinfinite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import victor.training.reactive.Utils;

import java.util.Collections;

@RestController
@SpringBootApplication
public class MigratingIMperative {
private LegacyImperativeBlockingCode legacy = new LegacyImperativeBlockingCode();

public static void main(String[] args) {
   Utils.installBlockHound(Collections.emptyList());

    SpringApplication.run(MigratingIMperative.class, args);
}


@GetMapping
   public Mono<String> method() {
      return yourCode("id")
          .flatMap(s -> wrappingLegacyBlockingCode(s));
   }

   public Mono<String> wrappingLegacyBlockingCode(String s) {
   // in a method returning Mono/Flux you ARE NOT ALLOWED TO :
      // block or throw exceptions.
      return Mono.fromSupplier(() -> legacy.innocent(s))
          .log("Above")
          .subscribeOn(Schedulers.boundedElastic())
          .log("Bellow")
//          .flatMap(a -> fff(a))
          ;
   }

   private Mono<String> yourCode(String s) {
//      return WebClient.....;
      return Mono.just(s);
   }
}


class LegacyImperativeBlockingCode {
   public String innocent(String s) {
      return blockingGet();
   }
   private String blockingGet() {
      Utils.sleep(100);
      return "data";
   }
}