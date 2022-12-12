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
   public CompletableFuture<DillyDilly> drink() throws Exception {
      log.info("Talking to barman: " + barman.getClass());
      // o executa un thread dintr-un thread pool al Tomcatului (app serverul de sub)
      long t0 = currentTimeMillis();

      // pornesc in executie in paralel cele 2 treburi pt ca sunt indep intre ele
      CompletableFuture<Beer> futureBeer = CompletableFuture.supplyAsync(() -> barman.pourBeer()); // by default ruleaza pe ForkJoin.commonPool are Ncpu-1 threaduri
      CompletableFuture<Vodka> futureVodka = CompletableFuture.supplyAsync(() -> barman.pourVodka());


// antipattern: .get pe CompletableFuture
//      Beer beer = futureBeer.get(); //cat timp sta blocat la ac linie threadul Tomcatului ? 1 sec
//      Vodka vodka = futureVodka.get();//cat timp sta blocat la ac linie threadul Tomcatului ? - 0, operatia deja s-a terminat

      CompletableFuture<DillyDilly> futureDilly = futureBeer.thenCombine(futureVodka, (beer, vodka) ->  barman.mixCocktail(beer, vodka));

//      DillyDilly dilly = barman.mixCocktail(beer, vodka);

      log.debug("HTTP thread was blocked for {} millis ", (currentTimeMillis() - t0));
      return futureDilly; // ii intorc lui Spring un future, el va sti sa astepte sa
   }

}

@Service
class Barman {
   private static final Logger log = LoggerFactory.getLogger(Barman.class);

   public Beer pourBeer() {
      log.info("Start beer");

      // 1: pretend
      sleep(1000);
      Beer beer = new Beer("blond");

      // 2: blocking REST call
//      Beer beer = new RestTemplate().getForEntity("http://localhost:9999/api/beer", Beer.class).getBody();
      log.info("End beer");
      return beer;

      // 3: non-blocking REST call
//      return new AsyncRestTemplate().exchange(...).completable()....;

      // 4: non-blocking REST call via WebClient
      // return WebClient.create().get()...
   }

   public Vodka pourVodka() {
      log.info("Start vodka");
      sleep(200);  // imagine blocking DB call
      log.info("End vodka");
      return new Vodka();
   }


   public DillyDilly mixCocktail(Beer beer, Vodka vodka) {
      log.info("Mixing {} with {} (takes time) ...", beer, vodka);
      sleep(500);
      return new DillyDilly(beer, vodka);
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