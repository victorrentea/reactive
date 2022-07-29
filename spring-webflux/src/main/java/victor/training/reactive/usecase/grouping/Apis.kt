package victor.training.reactive.usecase.grouping

import org.reactivestreams.Subscription
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import java.time.Duration

open class Apis {
    fun apiA(oddItem: Int): Mono<Void> {
        return api("A", oddItem)
    }

    fun apiB(oddItem: Int): Mono<Void> {
        return api("B", oddItem)
    }

    fun apiC(evenItemPage: List<Int>): Mono<Void> {
        return api("C", evenItemPage)
    }

    private fun api(apiName: String, data: Any): Mono<Void> {
        return Mono.delay(Duration.ofMillis(100))
            .doOnSubscribe { s: Subscription? ->
                log.info(
                    "Api$apiName: {} START", data
                )
            }
            .doOnTerminate { log.info("Api$apiName: {} END", data) }
            .then()
    }

    companion object {
        private val log = LoggerFactory.getLogger(Apis::class.java)
    }
}