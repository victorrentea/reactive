package victor.training.reactive.intro.mvc;

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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

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
      return "immediate"; // raspuns din memorie (eg din vreun HashMap, counter) 0.1%
   }

   @GetMapping("drink")
   public Mono<DillyDilly> drink() throws Exception { // 99% din cazuri intorci Mono/Flux
      log.info("Talking to barman: " + barman.getClass());

      long t0 = currentTimeMillis();
      Mono<Beer> futureBeer = barman.pourBeer();
      Mono<Vodka> futureVodka = barman.pourVodka();
      // JVM are un ForkJoinPool.commonPool default cu nCPU-1 threaduri in el.
      // tot pe el executa si .parallelStream() (- de evitat)
      // daca executi munca de IO pe un threadpool unic global per JVM poti suferi de
      // Thread Pool Starvation = nu e fair distributia threadurile. Taskuri blocheaza th comune.

//      Beer beer = futureBeer.get();// blocheaza threadul Tomcatului (1/200) pt 1 sec -- n-ai voie sa faci .get pe CF
      // de ce ?
      // CF = "promise" === CompletableFuture
//      Vodka vodka = futureVodka.get(); // 0 ms blocat

      // Tomcat nu mai e cu noi. Netty este acum sub. Un app server non-blocant.
      // N CPU * 10 threaduri = 100

      Mono<DillyDilly> futureDilly = futureBeer.zipWith(futureVodka, (beer, vodka) -> new DillyDilly(beer, vodka));

      log.debug("HTTP thread was blocked for {} millis ", (currentTimeMillis() - t0));
      return futureDilly;
   }
}

@Service
class Barman {
   private static final Logger log = LoggerFactory.getLogger(Barman.class);

   public Mono<Beer> pourBeer() {
//      log.info("Start beer"); // NU AICI se trimite de fapt GET-ul pe retea
      // log mincinos !!!
      Mono<Beer> beerMono = WebClient.create().get().uri("http://localhost:9999/api/beer")
              .retrieve().bodyToMono(Beer.class)

              .name("Beer-Flux")
              .metrics()

              .doOnSubscribe(s -> log.info("Start beer"))
              .doOnNext(e -> log.info("End beer : " + e))
              .doOnTerminate(() -> log.info("End beer anyway + errros")) //
              ;

//      log.info("End beer"); // NU AICI a venit raspunsul!
      return beerMono;
   }

   public Mono<Vodka> pourVodka() {
      log.info("Start vodka");
//      sleep(200);  // imagine blocking DB call
      log.info("End vodka");
      return Mono.just(new Vodka()).delayElement(Duration.ofMillis(200));
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
      // repo call blocant, rest Template ; orice bloca threadul
//      sleep(500); // NU ARE VOIE SA SE INTAMPLE AICI: sunt intr-un thread in care nu am voie sa ma blochez. (nu boundedElastic)
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