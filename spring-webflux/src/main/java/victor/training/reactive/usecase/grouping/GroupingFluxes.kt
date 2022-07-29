package victor.training.reactive.usecase.grouping

import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class GroupingFluxes(private val apis: Apis) {
    val log = LoggerFactory.getLogger(GroupingFluxes::class.java)

    fun processMessageStream(messageStream: Flux<Int>): Mono<Void> {
        //Depending on the message type, run one of the following flows:
        //TYPE1: Do nothing (ignore the message)
        //TYPE2: Call apiA(message) and apiB(message) [in parallel]
        //TYPE3: Call apiC(List.of(message)

        //TYPE3(HARD): Call apiC(messageList), buffering together requests such that
        //HARD: send max 3 IDs, but an ID waits max 500 millis
        return messageStream
//            .filter{MessageType.forMessage(it) != MessageType.TYPE1_NEGATIVE}
            .groupBy { MessageType.forMessage(it) }
            .flatMap { gf ->
                when (gf.key()) {
                    MessageType.TYPE1_NEGATIVE -> Mono.empty()
                    MessageType.TYPE2_ODD -> gf.flatMap { e2 -> apis.apiA(e2).then(apis.apiB(e2)) }
                    MessageType.TYPE3_EVEN -> gf.buffer(10).flatMap { e3Page -> apis.apiC(e3Page) }
//                    default-> nu ai voie!! doar exce[ptii nu "nimic"
                }
            }
            .then()
    }
}

fun main(args: Array<String>) {
    val messageStream = Flux.range(0, 10)
    //          Flux.interval(ofMillis(200)).map(Long::intValue).take(10);
    GroupingFluxes(Apis()).processMessageStream(messageStream).block()
}
