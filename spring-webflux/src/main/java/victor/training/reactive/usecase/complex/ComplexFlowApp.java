package victor.training.reactive.usecase.complex;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuples;

import java.util.List;
import java.util.stream.LongStream;

import static java.lang.System.currentTimeMillis;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static victor.training.reactive.Utils.installBlockHound;
import static victor.training.reactive.Utils.sleep;

@RestController
@Slf4j
@SpringBootApplication
public class ComplexFlowApp implements CommandLineRunner {
   public static void main(String[] args) {
      SpringApplication.run(ComplexFlowApp.class);
   }


   // CE vezi mai jos e o mizerie!
   // e doar pt debugging. e un filtru care obliga toate req http sa execute pe .parallel nu pe Schedulerul netty
   // pentru ca Blockhound (care vaneaza .block()) arunca EX DOAR DACA blochezi in .parallel
   // nu pt prod
   @Bean
   public WebFilter alwaysParallelWebfluxFilter() {
      // ⚠️ WARNING: use this only when exploring the non-block-ness of your code. Inspired from: https://gitter.im/reactor/BlockHound?at=5dc023b4e1c5e91508300190
      installBlockHound(List.of(
              Tuples.of("io.netty.resolver.HostsFileParser", "parse"),
              Tuples.of("victor.training.reactive.reactor.complex.ComplexFlowMain", "executeAsNonBlocking")
      ));
      return (exchange, chain) -> Mono.defer(() -> chain.filter(exchange))
//              .subscribeOn(Schedulers.single()); // welcome to NodeJS
              .subscribeOn(Schedulers.parallel()); // ia req de pe schedulerul netty default is il subscrie pe .parallle()
   }

   @Override
   public void run(String... args) throws Exception {
      Hooks.onOperatorDebug(); // provide better stack traces
      log.info("Calling myself automatically once at startup");
      WebClient.create().get().uri("http://localhost:8080/complex").retrieve().bodyToMono(String.class)
          .subscribe(
              data -> log.info("COMPLETED with: "+data),
              error -> log.error("FAILED! See above why: "+ error )
          );
   }

   @GetMapping("complex")
   public Mono<String> executeAsNonBlocking(@RequestParam(value = "n", defaultValue = "10") int n) {
      long t0 = currentTimeMillis();
      List<Long> productIds = LongStream.rangeClosed(1, n).boxed().collect(toList());

      log.info("Pe ce thread sunt aici?");
//      sleep(100);// BUM block hound latra aici !
      log.info("Dupa sleep. am paralizat 1 thread netty");

      Mono<List<Product>> listMono = complexFlow.mainFlow(productIds);

      return listMono.map(list ->
              "<h2>Done!</h2>\n" +
              "Requested " + n + " (add ?n=1000 to url to change), " +
              "returning " + list.size() + " products " +
              "after " + (currentTimeMillis() - t0) + " ms: <br>\n<br>\n" +
              list.stream().map(Product::toString).collect(joining("<br>\n")))
              .doOnNext(s -> log.info("Da aici ?"));
   }

   @Autowired
   private ComplexFlowSolved complexFlow;



}

