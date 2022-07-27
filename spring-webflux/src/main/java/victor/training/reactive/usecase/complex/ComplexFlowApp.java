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

@RestController
@Slf4j
@SpringBootApplication
public class ComplexFlowApp implements CommandLineRunner {
   public static void main(String[] args) {
      SpringApplication.run(ComplexFlowApp.class, "--server.port=8081");
   }

   @Bean
   public WebFilter alwaysParallelWebfluxFilter() {
      // ⚠️ WARNING: use this only when exploring the non-block-ness of your code. Inspired from: https://gitter.im/reactor/BlockHound?at=5dc023b4e1c5e91508300190
      installBlockHound(List.of(
              Tuples.of("io.netty.resolver.HostsFileParser", "parse"),
              Tuples.of("victor.training.reactive.reactor.complex.ComplexFlowMain", "executeAsNonBlocking")
      ));
      return (exchange, chain) -> Mono.defer(() -> chain.filter(exchange)).subscribeOn(Schedulers.parallel());
   }

   @Override
   public void run(String... args) throws Exception {

      Hooks.onOperatorDebug(); // provide better stack traces

      log.info("Calling myself automatically once at startup");
      WebClient.create().get().uri("http://localhost:8081/complex").retrieve().bodyToMono(String.class)
          .subscribe(
              data -> log.info("COMPLETED with: "+data),
              error -> log.error("FAILED! See above why" )
          );
   }

   @GetMapping("complex")
   public Mono<String> executeAsNonBlocking(@RequestParam(value = "n", defaultValue = "100") int n) {
      long t0 = currentTimeMillis();
      List<Long> productIds = LongStream.rangeClosed(1, n).boxed().collect(toList());

      Mono<List<Product>> listMono = complexFlow.mainFlow(productIds);

      return listMono.map(list ->
              "<h2>Done!</h2>\n" +
              "Requested " + n + " (add ?n=1000 to url to change), " +
              "returning " + list.size() + " products " +
              "after " + (currentTimeMillis() - t0) + " ms: <br>\n<br>\n" +
              list.stream().map(Product::toString).collect(joining("<br>\n")));
   }

   @Autowired
   private ComplexFlow complexFlow;



}

