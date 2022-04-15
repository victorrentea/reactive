package victor.training.reactive.demo.mdc_lifter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import static java.time.Duration.ofSeconds;

@RestController
@SpringBootApplication
public class MDCLifterApp {
   private static final Logger log = LoggerFactory.getLogger(MDCLifterApp.class);

   public static void main(String[] args) {
       SpringApplication.run(MDCLifterApp.class, args);
   }

   @GetMapping
   public Mono<String> mdcDemo() {
      log.debug("In subscriber thread");
      return Mono.just("Stuff")
          .doOnEach(v -> log.debug("Start " + v))
          .doOnNext(e -> f())
          .delayElement(ofSeconds(1)) // publishes on parallel scheduler
          .doOnEach(v -> log.debug("End " + v))
          ;
   }

   private void f() {
      log.debug("In called function");
   }
}
