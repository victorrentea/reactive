package victor.training.reactive.usecase.coroutine

import reactor.core.publisher.Mono
import reactor.function.TupleUtils
import reactor.util.function.Tuples

class MultipleSubscribersMono_Zip3(private val apis: Apis) {

//    data class Shaworma(val a:A, val price:B?, cc )
    // from id ==> get A
    // from A ==> get B
    // from A and B ==> get C
    suspend fun  retrieveC(id: Long): Apis.C {
        val a = apis.getA(id)
        val b = apis.getB(a)
        return  apis.getC(a,b)
    }


}