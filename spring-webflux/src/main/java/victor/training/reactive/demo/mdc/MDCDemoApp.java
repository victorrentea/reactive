package victor.training.reactive.demo.mdc;

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
public class MDCDemoApp {
   private static final Logger log = LoggerFactory.getLogger(MDCDemoApp.class);

   public static void main(String[] args) {
       SpringApplication.run(MDCDemoApp.class, args);
   }

   @GetMapping
   public Mono<String> mdcDemo() {

      return Mono.just("Stuff")
          .doOnEach(LogbackMDC.logOnNext(v -> log.debug("Start " + v)))
          .delayElement(ofSeconds(1))
          .doOnEach(LogbackMDC.logOnNext(v -> log.debug("End " + v)))
          ;
   }

}
