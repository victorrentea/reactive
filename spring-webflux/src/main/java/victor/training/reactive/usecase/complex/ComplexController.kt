package victor.training.reactive.usecase.complex

import io.micrometer.core.annotation.Timed
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Hooks
import reactor.core.publisher.Mono
import java.lang.IllegalArgumentException
import java.util.stream.Collectors

@RestController

class ComplexController(
    val complexFlow: ComplexFlow
) : CommandLineRunner {
    private val log = LoggerFactory.getLogger(ComplexController::class.java)
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
    fun executeAsNonBlocking(@RequestParam(value = "n", defaultValue = "10") n: Int): Mono<String> {
        val t0 = System.currentTimeMillis()
        val productIds = (0L until n).toList()
        val listMono = complexFlow.mainFlow(productIds).collectList()

        return listMono.map {
            val delta = System.currentTimeMillis() - t0

            """<h2>Done!</h2>
            Requested $n (add ?n=1000 to url to change), returning ${it.size} products after $delta ms: <br>
            <br>
            ${it.stream().map(Product::toString).collect(Collectors.joining("<br>\n"))}"""
        }
    }

    @GetMapping("err")
    fun met():Mono<String> {
        return Mono.error( IllegalArgumentException("vai di mine"))

    }

}