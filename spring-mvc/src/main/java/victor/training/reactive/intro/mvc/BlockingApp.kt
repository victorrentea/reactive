package victor.training.reactive.intro.mvc;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
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
      return registry -> registry.config()
          .commonTags(
              "application", "spring-mvc",
              "region", "training-region");
   }


   public static void main(String[] args) {
      org.springframework.boot.SpringApplication.run(BlockingApp.class, args);
   }

   @Autowired
   private Barman barman;

   @GetMapping("fast")
   public String undeTalciokul() throws Exception {
      return "immediate dupa colt";
   }

   @GetMapping("drink")
   public CompletableFuture<Yorsh> drink() throws Exception {
      log.info("Talking to barman: " + barman.getClass());

      long t0 = currentTimeMillis();

      CompletableFuture<Beer> beerPromise =  barman.pourBeer();
      CompletableFuture<Vodka> vodkaPromise = barman.pourVodka();


      CompletableFuture<Yorsh> yorshPromise = beerPromise.thenCombineAsync(vodkaPromise,
              (b,v) -> new Yorsh(b,v));


//      Yorsh yorsh = yorshPromise.get();
      long deltaT = currentTimeMillis() - t0;
      log.info("HTTP thread was blocked for {} millis ", deltaT);
      return yorshPromise;
   }
}

@Service
class Barman {
   private static final Logger log = LoggerFactory.getLogger(Barman.class);
// "promise" (JS) = = = CompletableFuture (java)
   @Async
   public CompletableFuture<Beer> pourBeer() {
      log.info("Start pour beer");

      // 1: emulate REST Call
      sleep(1000);
      Beer beer = new Beer("blond");

      // 2: really call
//      Beer beer = new RestTemplate().getForEntity("http://localhost:9999/api/beer", Beer.class).getBody();

      // 3: async alternative
//      return new AsyncRestTemplate().exchange()
//          .completable().thenApply(tranform);

      log.info("End pour beer");
      return CompletableFuture.completedFuture(beer);
   }

   @Async
   public CompletableFuture<Vodka> pourVodka() {
      log.info("Start pour vodka");
      sleep(200);  // imagine blocking DB call
      log.info("End pour vodka");
      return CompletableFuture.completedFuture(new Vodka());
   }
}


class Beer {
   private String type;
   public Beer() {}
   Beer(String type) {
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

class Yorsh {
   private static final Logger log = LoggerFactory.getLogger(Yorsh.class);
   private final Beer beer;
   private final Vodka vodka;

   public Yorsh(Beer beer, Vodka vodka) {
      this.beer = beer;
      this.vodka = vodka;
      log.info("Mixing {} with {} (takes time) ...", beer, vodka);
      sleep(500);
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