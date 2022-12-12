package victor.training.reactive.intro.mvc;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static java.lang.System.currentTimeMillis;
import static victor.training.reactive.intro.mvc.Utils.sleep;

@EnableAsync
@RestController
@SpringBootApplication
public class BlockingApp {

   private static final Logger log = LoggerFactory.getLogger(BlockingApp.class);

   @Bean
   MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
      // expose tomcat metrics (threads, connections) to prometheus via /actuator/prometheus
      return registry -> registry.config().commonTags(
              "application", "spring-mvc",
              "region", "training-region");
   }


   public static void main(String[] args) {
      org.springframework.boot.SpringApplication.run(BlockingApp.class, args);
   }

   @Autowired
   private Barman barman;

   @GetMapping("fast")
   public String fast() {
      return "immediate";
   }

   @GetMapping("drink")
   public Mono<DillyDilly> drink() throws Exception {
      log.info("Talking to barman: " + barman.getClass());
      long t0 = currentTimeMillis();

      // pornesc in executie in paralel cele 2 treburi pt ca sunt indep intre ele
      Mono<Beer> futureBeer = barman.pourBeer(); // by default ruleaza pe ForkJoin.commonPool are Ncpu-1 threaduri
      Mono<Vodka> futureVodka = barman.pourVodka();

      Mono<DillyDilly> dillyMono = futureBeer
              .zipWith(futureVodka, (beer, vodka) -> barman.mixCocktail(beer, vodka))
              .flatMap(m -> m);

      log.debug("HTTP thread was blocked for {} millis ", (currentTimeMillis() - t0));
      return dillyMono; // ii intorc lui Spring un future, el va sti sa astepte sa
   }
//   @GetMapping("drink")
//   public async DillyDilly drinkAlaNode() throws Exception {
//      Beer futureBeer = await barman.pourBeer(); // by default ruleaza pe ForkJoin.commonPool are Ncpu-1 threaduri
//      Vodka futureVodka = await barman.pourVodka();
//      DillyDilly dilly = await barman.mixCocktail(beer, vodka);
//      return dilly;
//    }

}

@Service
class Barman {
   private static final Logger log = LoggerFactory.getLogger(Barman.class);

   public Mono<Beer> pourBeer() {
      String vai = UUID.randomUUID().toString();
      //      log.info("Start beer"); // aceste loguri MINT aici
      Mono<Beer> beerMono = WebClient.create().get()
              .uri("http://localhost:9999/api/beer")
              .retrieve().bodyToMono(Beer.class)
              .doOnSubscribe(s -> log.info("Start beer " + vai))
              .doOnSuccess(beer->log.info("End beer "+vai + ": " +beer))
              ;
//      log.info("End beer"); // aceste loguri MINT aici
      return beerMono;
   }

   public Mono<Vodka> pourVodka() {
      log.info("Start vodka");
//      sleep(200);  // imagine blocking DB call
      log.info("End vodka");
      return Mono.just(new Vodka()).delayElement(Duration.ofMillis(200));
   }


   public Mono<DillyDilly> mixCocktail(Beer beer, Vodka vodka) {
      log.info("Mixing {} with {} (takes time) ...", beer, vodka);
//      sleep(500);
      return Mono.just(new DillyDilly(beer, vodka)).delayElement(Duration.ofMillis(500));
   }
}


class Beer {
   private String type;
   public Beer() {}
   public Beer(String type) {
      this.type = type;
   }
   public String getType() {
      return type;
   }
}

class Vodka {
   private final String type = "deadly";
   public String getType() {
      return type;
   }
}

class DillyDilly {
   private static final Logger log = LoggerFactory.getLogger(DillyDilly.class);
   private final Beer beer;
   private final Vodka vodka;

   public DillyDilly(Beer beer, Vodka vodka) {
      this.beer = beer;
      this.vodka = vodka;
   }

   public Beer getBeer() {
      return beer;
   }

   public Vodka getVodka() {
      return vodka;
   }
}

class Utils {
   public static void sleep(long millis) {
      try {
         Thread.sleep(millis);
      } catch (InterruptedException e) {
         throw new RuntimeException(e);
      }
   }
}