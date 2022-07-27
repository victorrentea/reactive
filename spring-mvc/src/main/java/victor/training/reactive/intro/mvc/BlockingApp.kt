package victor.training.reactive.intro.mvc

import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.AsyncRestTemplate
import org.springframework.web.client.RestTemplate
import java.util.concurrent.CompletableFuture

@EnableAsync
@RestController
@SpringBootApplication
open class BlockingApp {
    @Bean
    open fun metricsCommonTags(): MeterRegistryCustomizer<MeterRegistry> {
        return MeterRegistryCustomizer { registry: MeterRegistry ->
            registry.config()
                .commonTags(
                    "application", "spring-mvc",
                    "region", "training-region"
                )
        }
    }

    @Autowired
    private val barman: Barman? = null

    @GetMapping("fast")
    @Throws(Exception::class)
    fun undeTalciokul(): String {
        return "immediate dupa colt"
    }

    @GetMapping("drink")
    @Throws(Exception::class)
    fun drink(): CompletableFuture<Yorsh> {
        log.info("Talking to barman: " + barman!!.javaClass)
        val t0 = System.currentTimeMillis()
        val beerPromise = barman.pourBeer()
        val vodkaPromise = barman.pourVodka()
        val yorshPromise = beerPromise.thenCombineAsync(
            vodkaPromise
        ) { b: Beer?, v: Vodka? -> Yorsh(b, v) }

        val deltaT = System.currentTimeMillis() - t0
        log.info("HTTP thread was blocked for {} millis ", deltaT)
        return yorshPromise
    }

    companion object {
        private val log = LoggerFactory.getLogger(BlockingApp::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(BlockingApp::class.java, *args)
        }
    }
}

@Service
open class Barman {
    // "promise" (JS) = = = CompletableFuture (java)
//    @Async
    open fun pourBeer(): CompletableFuture<Beer> {
        log.info("Start pour beer")

        val beer = AsyncRestTemplate()
            .getForEntity("http://localhost:9999/api/beer", Beer::class.java)
            .completable()
            .thenApply {
                log.info("End pour beer")
                it.body!! }

        return beer
    }

    @Async
    open fun pourVodka(): CompletableFuture<Vodka> {
        log.info("Start pour vodka")
        Utils.sleep(200) // imagine blocking DB call
        log.info("End pour vodka")
        return CompletableFuture.completedFuture(Vodka())
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