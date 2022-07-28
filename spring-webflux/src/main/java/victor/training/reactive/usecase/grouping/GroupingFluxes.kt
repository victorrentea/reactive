package victor.training.reactive.usecase.grouping

import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class GroupingFluxes(private val apis: Apis) {
    val log = LoggerFactory.getLogger(GroupingFluxes::class.java)

    fun processMessageStream(messageStream: Flux<Int?>): Mono<Void> {
        //Depending on the message type, run one of the following flows:
        //TYPE1: Do nothing (ignore the message)
        //TYPE2: Call apiA(message) and apiB(message) in parallel
        //TYPE3: Call apiC(List.of(message)

        //TYPE3(HARD): Call apiC(messageList), buffering together requests such that
        //HARD: send max 3 IDs, but an ID waits max 500 millis
        return messageStream
            .then()
    }
}

fun main(args: Array<String>) {
    val messageStream = Flux.range(0, 10)
    //          Flux.interval(ofMillis(200)).map(Long::intValue).take(10);
    GroupingFluxes(Apis()).processMessageStream(messageStream).block()
}
