package victor.training.reactive.usecase.complex

import io.micrometer.core.annotation.Timed
import io.micrometer.core.aop.TimedAspect
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Hooks
import reactor.core.publisher.Mono
import victor.training.reactive.Utils
import java.util.stream.Collectors.joining

@RestController
@SpringBootApplication
@EnableCaching

class ComplexFlowApp : CommandLineRunner {

    private val log = LoggerFactory.getLogger(ComplexFlowApp::class.java)

    // enables the use of @Timed (exposed via /actuator/prometheus)
    @Bean
    fun timedAspect(meterRegistry: MeterRegistry?): TimedAspect {
        return TimedAspect(meterRegistry)
    }

    //    @Bean
    //    public WebFilter alwaysParallelWebfluxFilter() {
    //        // ⚠️ WARNING: use this only when exploring the non-block-ness of your code.
    //        installBlockHound(List.of(
    //                Tuples.of("io.netty.resolver.HostsFileParser", "parse"),
    //                Tuples.of("victor.training.reactive.reactor.complex.ComplexFlowMain", "executeAsNonBlocking")
    //        ));
    //        return (exchange, chain) -> Mono.defer(() -> chain.filter(exchange)).subscribeOn(Schedulers.parallel());
    //    }

    @Autowired
    lateinit var webClient: WebClient

    @Throws(Exception::class)
    override fun run(vararg args: String) {
        Hooks.onOperatorDebug() // provide better stack traces
        log.info("Calling myself automatically once")
        webClient.get().uri("http://localhost:8080/complex").retrieve().bodyToMono(
            String::class.java
        )
            .subscribe(
                { data: String -> log.info("Call COMPLETED with: $data") }
            ) { error: Throwable -> log.error("Call to MainFlow FAILED! See above why: $error") }
    }

    @GetMapping("complex")
    @Timed("complex") // @TimedReactive // TODO
    fun executeAsNonBlocking(@RequestParam(value = "n", defaultValue = "100") n: Int): Mono<String> {
        val t0 = System.currentTimeMillis()
        val productIds = (0L until n).toList()
        val listMono = complexFlow.mainFlow(productIds).collectList()

        return listMono.map {
            val delta = System.currentTimeMillis() - t0

            """<h2>Done!</h2>
            Requested $n (add ?n=1000 to url to change), returning ${it.size} products after $delta ms: <br>
            <br>
            ${it.stream().map(Product::toString).collect(joining("<br>\n"))}"""
        }
    }


    @Autowired
    lateinit var complexFlow: ComplexFlow

    @Bean
    fun webClient(): WebClient = WebClient.create()

}
fun main() {
    SpringApplication.run(ComplexFlowApp::class.java)
}
