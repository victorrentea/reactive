package victor.training.reactive.usecase.zipchain

import reactor.core.publisher.Mono
import reactor.util.function.Tuples

class MultipleSubscribersMono_Zip3(private val apis: Apis) {

//    data class Shaworma(val a:A, val price:B?, cc )
    // from id ==> get A
    // from A ==> get B
    // from A and B ==> get C
    fun retrieveC(id: Long): Mono<Apis.C> {
        // dupa 2 zile de debug tragi o concluzie: niciodata nu tii Mono/Flux in variabile,
        // ca maine cineva vine tiptil si face si el pe acelasi mono.flatMap
        // daca chainuiesti de 2 ori din acelasi subscriber >>>>>>>>> 2 calluri de retea invizibile

    return apis.getA(id)
        .zipWhen({ a -> apis.getB(a) }, { a, b -> apis.getC(a, b) })
        .flatMap { it }

//    return apis.getA(id)
//            .flatMap { a-> apis.getB(a).map{ b -> a to b} }
//            .flatMap { (a,b) -> apis.getC(a,b) }



//        val monoA = apis.getA(id)
//
//        val monoB = monoA.flatMap { a ->
//            apis.getB(a)
//        }

//         return monoA.zipWith(monoB) {a,b ->
//            apis.getC(a,b)
//        }.flatMap{it}
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