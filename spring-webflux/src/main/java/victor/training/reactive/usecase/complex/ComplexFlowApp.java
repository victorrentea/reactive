package victor.training.reactive.usecase.complex;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

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
      List<Long> productIds = LongStream.rangeClosed(1, 1000_000).boxed().collect(toList());


      return mainFlow(productIds)
           .collectList()
          .map(list -> "Done. Got " + list.size() + " products: " + list);
   }

   // TIP: Caching in Reactor: https://stackoverflow.com/questions/48156424/spring-webflux-and-cacheable-proper-way-of-caching-result-of-mono-flux-type
   // ================================== work below ====================================

   public Flux<Product> mainFlow(List<Long> productIds) {
       return Flux.fromIterable(productIds)
              .buffer(2)
              .flatMap(ComplexFlowApp::retrieveMany, 10)
//              .delayUntil(ComplexFlowApp::auditResealed)
               .doOnNext(p -> auditResealed(p).subscribe() // pierzi CANCEL signal
        // daca subscriberu final da cancel audit resealed deja lansate nu poti sa le cancelezi.
               )
            ;
       // Q de la biz: nu ne pasa de erorile de la audit
      // problema: auditu dureaza si nu are sens sa stam dupa el. Sa facem deci fire-and-forget
   }

   @NotNull
   private static Mono<Void> auditResealed(Product p) {
      if (p.isResealed()) {
         return ExternalAPIs.auditResealedProduct(p)
                 .doOnError(e -> log.error("Ca m-am panicat", e))
                 .onErrorResume(e -> Mono.empty())   // catch (Exception) {log;}
                 ;
      } else {
         return Mono.empty();
      }
   }

   // perfect
   private static Flux<Product> retrieveMany(List<Long> productIds) {
      log.info("Call pentru " + productIds);
      return WEB_CLIENT
              .post()
              .uri("http://localhost:9999/api/product/many")
              .bodyValue(productIds)
              .retrieve()
              .bodyToFlux(ProductDetailsResponse.class) // jackson parseaza progresiv JSONu cum vine si-ti emite semnale de date ProductDetails.
              .map(ProductDetailsResponse::toEntity)
      ;
   }



}

