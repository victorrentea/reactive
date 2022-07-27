package victor.training.reactive.usecase.complex;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;

import static java.util.stream.Collectors.toList;
import static victor.training.reactive.Utils.installBlockHound;

@RestController
@Slf4j
@SpringBootApplication
public class ComplexFlowApp implements CommandLineRunner {

   public static final WebClient WEB_CLIENT = WebClient.create();

   public static void main(String[] args) {
      SpringApplication.run(ComplexFlowApp.class, "--server.port=8081");
   }

   @EventListener(ApplicationStartedEvent.class)
   public void setupBlockingDetection() {
      installBlockHound(List.of(
          Tuples.of("io.netty.resolver.HostsFileParser", "parse"),
          Tuples.of("victor.training.reactive.reactor.complex.ComplexFlowMain", "executeAsNonBlocking")
      ));
   }

   @Override
   public void run(String... args) throws Exception {

      Hooks.onOperatorDebug(); // provide better stack traces

      log.info("Calling myself automatically once");
      WEB_CLIENT.get().uri("http://localhost:8081/complex").retrieve().bodyToMono(String.class)
          .subscribe(
              data -> log.info("COMPLETED with: "+data),
              error -> log.error("FAILED! See above why" )
          );
//      startRefreshWireMockStubsFromJsonContinuously();
   }

   @GetMapping("complex")
   public Mono<String> executeAsNonBlocking() {
      List<Long> productIds = LongStream.rangeClosed(1, 1000).boxed().collect(toList());

      return mainFlow(productIds)
          .map(list -> "Done. Got " + list.size() + " products: " + list);
   }

   // TIP: Caching in Reactor: https://stackoverflow.com/questions/48156424/spring-webflux-and-cacheable-proper-way-of-caching-result-of-mono-flux-type
   // ================================== work below ====================================

   public Mono<List<Product>> mainFlow(List<Long> productIds) {
      return Flux.fromIterable(productIds)
              // o forma primitiva de rate limiting :
              .flatMap(ComplexFlowApp::retrieve, 10)
              .collectList();
   }

   // perfect
   private static Mono<Product> retrieve(Long productId) {
      log.info("Call pentru " + productId);
      return WEB_CLIENT
              .get()
              .uri("http://localhost:9999/api/product/" + productId)
              .retrieve()
              .bodyToMono(ProductDetailsResponse.class)
              .map(ProductDetailsResponse::toEntity)
      ;
   }


}

