package victor.training.reactive.usecase.zipchain

import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import victor.training.reactive.Utils

class Apis {
    fun getA(id: Long): Mono<A> {
        return Mono.fromCallable {
            log.info("getA() -- Sending expensive REST call...")
            Utils.sleep(1000)
            A()
        }
    }

    fun getB(a: A?): Mono<B> {
        return Mono.fromCallable {
            log.info("getB({})", a)
            B()
        }
    }

    fun getC(a: A?, b: B?): Mono<C> {
        return Mono.fromCallable {
            log.info("getC({},{})", a, b)
            C()
        }
    }

    class A
    class B
    class C
    companion object {
        private val log = LoggerFactory.getLogger(Apis::class.java)
    }
}