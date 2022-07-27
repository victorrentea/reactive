package victor.training.reactive.intro

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.time.Duration

@EnableAsync
@RestController
@SpringBootApplication
open class NonBlockingAppCuWebFlux {
    @Autowired
    private val barman: Barman? = null

    @GetMapping("fast")
    fun undeTalciokul(): Mono<String> = Mono.just("immediate dupa colt")

    @GetMapping("drink")
    fun drink(): Mono<Yorsh> {
        log.info("Talking to barman: " + barman!!.javaClass)
        val t0 = System.currentTimeMillis()
        val beerMono = barman.pourBeer()
        val vodkaMono = barman.pourVodka()
        val yorshMono = beerMono.zipWith(vodkaMono) {b,v ->Yorsh(b,v)}

        val deltaT = System.currentTimeMillis() - t0
        log.info("HTTP thread was blocked for {} millis ", deltaT)
        return yorshMono
    }

    companion object {
        private val log = LoggerFactory.getLogger(NonBlockingAppCuWebFlux::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(NonBlockingAppCuWebFlux::class.java, *args)
        }
    }
}

@Service
open class Barman {
    // "promise" (JS) = = = CompletableFuture (java)
//    @Async
    open fun pourBeer(): Mono<Beer> {
        log.info("Start pour beer")

        val beer = WebClient.create()
            .get()
            .uri("http://localhost:9999/api/{1}","beer")
            .exchangeToMono{ it.bodyToMono(Beer::class.java)}
        return beer
    }

    open fun pourVodka(): Mono<Vodka> {
        log.info("Start pour vodka")
//        Utils.sleep(200) // imagine blocking DB call
        log.info("End pour vodka")
        return Mono.delay(Duration.ofMillis(200))
//            .map { Vodka() } // ok dar folosesti un op un pic prea puternic pentru ce ai de facut.
            .thenReturn(Vodka())

//        return Mono.just(Vodka())
//            .delayElement(Duration.ofMillis(200))
    }

    companion object {
        private val log = LoggerFactory.getLogger(Barman::class.java)
    }
}

class Beer {
    var type: String? = null
        private set

    constructor() {}
    constructor(type: String?) {
        this.type = type
    }
}

class Vodka {
    val type = "deadly"
}

class Yorsh(val beer: Beer?, val vodka: Vodka?) {

    init {
        log.info("Mixing {} with {} (takes time) ...", beer, vodka)
        Utils.sleep(500)
    }

    companion object {
        private val log = LoggerFactory.getLogger(Yorsh::class.java)
    }
}

internal object Utils {
    fun sleep(millis: Long) {
        try {
            Thread.sleep(millis)
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
    }
}