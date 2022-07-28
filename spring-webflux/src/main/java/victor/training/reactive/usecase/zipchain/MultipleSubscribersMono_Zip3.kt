package victor.training.reactive.usecase.zipchain

import reactor.core.publisher.Mono

class MultipleSubscribersMono_Zip3(private val apis: Apis) {

    // from id ==> get A
    // from A ==> get B
    // from A and B ==> get C
    fun retrieveC(id: Long): Mono<Apis.C> {
        // TODO fix
        val monoA = apis!!.getA(id)
        val monoB = apis.getB(null)
        return apis.getC(null, null)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val c = MultipleSubscribersMono_Zip3(Apis())
                .retrieveC(1L).blockOptional().orElseThrow()
            println("Got C = $c")
        }
    }
}