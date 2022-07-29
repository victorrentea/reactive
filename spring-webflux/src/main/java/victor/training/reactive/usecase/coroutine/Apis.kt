package victor.training.reactive.usecase.coroutine

import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import victor.training.reactive.Utils

interface Apis {
    suspend fun  getA(id: Long): A;

    suspend fun getB(a: A?): B;

    suspend fun getC(a: A?, b: B?):C;

    class A
    class B
    class C
    companion object {
        private val log = LoggerFactory.getLogger(Apis::class.java)
    }
}