package victor.training.reactive.usecase.complex;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuples;
import victor.training.reactive.Utils;

import java.time.Duration;
import java.util.List;
import java.util.stream.LongStream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static victor.training.reactive.Utils.installBlockHound;
import static victor.training.reactive.Utils.sleep;

@RestController
@Slf4j
@SpringBootApplication
public class ComplexFlowApp implements CommandLineRunner {

    public static final WebClient WEB_CLIENT = WebClient.create();

    public static void main(String[] args) {
        SpringApplication.run(ComplexFlowApp.class, "--server.port=8081");
    }

    @Bean
    public WebFilter alwaysParallelWebfluxFilter() {
        // ⚠️ WARNING: use this only when exploring the non-block-ness of your code.
        installBlockHound(List.of(
                Tuples.of("io.netty.resolver.HostsFileParser", "parse"),
                Tuples.of("victor.training.reactive.reactor.complex.ComplexFlowMain", "executeAsNonBlocking")
        ));
        return (exchange, chain) -> Mono.defer(() -> chain.filter(exchange)).subscribeOn(Schedulers.parallel());
    }
    @Override
    public void run(String... args) throws Exception {

        Hooks.onOperatorDebug(); // provide better stack traces

        log.info("Calling myself automatically once");
        WEB_CLIENT.get().uri("http://localhost:8081/complex").retrieve().bodyToMono(String.class)
                .subscribe(
                        data -> log.info("Call COMPLETED with: " + data),
                        error -> log.error("Call to MainFlow FAILED! See above why")
                );
    }

    @GetMapping("complex")
    public Mono<String> executeAsNonBlocking(@RequestParam(value = "n", defaultValue = "100") int n) {
        List<Long> productIds = LongStream.rangeClosed(1, n).boxed().collect(toList());

        Mono<List<Product>> listMono = mainFlow(productIds)
                .collectList();

        return listMono.map(list ->
                "<h2>Done!</h2>\nRequested " + n +" (add ?n=1000 to url to change), returning " + list.size() + " products: <br>\n<br>\n" +
                             list.stream().map(Product::toString).collect(joining("<br>\n")));
    }


    public Flux<Product> mainFlow(List<Long> productIds) {
        return Flux.fromIterable(productIds)
                .buffer(2)
                .flatMap(ComplexFlowApp::retrieveMany, 10)
//              .delayUntil(ComplexFlowApp::auditResealed)
                .doOnNext(ComplexFlowApp::auditResealed // pierzi CANCEL signal
                        // daca subscriberu final da cancel audit resealed deja lansate nu poti sa le cancelezi.
                )
                ;
        // Q de la biz: nu ne pasa de erorile de la audit
        // problema: auditu dureaza si nu are sens sa stam dupa el. Sa facem deci fire-and-forget
    }

    private static void auditResealed(Product p) {
        if (p.isResealed()) {
            ExternalAPIs.auditResealedProduct(p)
                    .subscribe(Utils::noop, Utils::handleError);
        }
    }

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

