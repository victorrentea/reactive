package victor.training.reactive.intro.mvc;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
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
   public ThreadPoolTaskExecutor executor() {
      ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
      executor.setCorePoolSize(400);
      executor.setMaxPoolSize(400);
      executor.setQueueCapacity(500);
      executor.setThreadNamePrefix("bar-");
      executor.initialize();
      return executor;
   }

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

}
@RestController
class X {

   private static final Logger log = LoggerFactory.getLogger(X.class);
   @Autowired
   private Barman barman;

   @Autowired
   ThreadPoolTaskExecutor threadPool;

   @GetMapping("fast")
   public String fast() {
      return "immediate";
   }

   @GetMapping("drink")
   public CompletableFuture<DillyDilly> drink() throws Exception {
      log.info("Talking to barman: " + barman.getClass());

      long t0 = currentTimeMillis();
      CompletableFuture<Beer> futureBeer = CompletableFuture.supplyAsync(() -> barman.pourBeer(), threadPool);
      CompletableFuture<Vodka> futureVodka = CompletableFuture.supplyAsync(() -> barman.pourVodka(), threadPool);
      // JVM are un ForkJoinPool.commonPool default cu nCPU-1 threaduri in el.
      // tot pe el executa si .parallelStream() (- de evitat)
      // daca executi munca de IO pe un threadpool unic global per JVM poti suferi de
      // Thread Pool Starvation = nu e fair distributia threadurile. Taskuri blocheaza th comune.

//      Beer beer = futureBeer.get();// blocheaza threadul Tomcatului (1/200) pt 1 sec -- n-ai voie sa faci .get pe CF
      // de ce ?
      // CF = "promise" === CompletableFuture
//      Vodka vodka = futureVodka.get(); // 0 ms blocat

      CompletableFuture<DillyDilly> futureDilly = futureBeer.thenCombine(futureVodka, (beer, vodka) -> new DillyDilly(beer, vodka));

      log.debug("HTTP thread was blocked for {} millis ", (currentTimeMillis() - t0));
      return futureDilly;
   }
}

@Service
class Barman {
   private static final Logger log = LoggerFactory.getLogger(Barman.class);

   @Timed("beer-api-call")
   public Beer pourBeer() {
      log.info("Start beer");
//if (true) throw new IllegalStateException("Nu mai e bere");
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