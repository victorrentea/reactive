package victor.training.reactive.usecase.debugging

import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class Apis {
    fun callGreen(): Mono<String> {
        return Mono.just("1")
    }

    fun callBlue(): Mono<String> {
        return Mono.just("one")
    }
}