package victor.training.reactive.usecase.monitoringinfinite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
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
          .map(s -> legacy.innocent(s));
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