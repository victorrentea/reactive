package victor.training.reactive.usecase.debugging

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@SpringBootApplication
class DebuggingApp(private val someService: SomeService, private val apis: Apis) {
    @GetMapping
    fun catch_me_if_you_can(): Mono<Int> {
        return Mono.zip(
            apis.callGreen().map { integer: String? ->
                someService.logic(
                    integer!!
                )
            },
            apis.callBlue().map { integer: String? ->
                someService.logic(
                    integer!!
                )
            }) { a: Int?, b: Int? -> Integer.sum(a!!, b!!) }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(DebuggingApp::class.java, *args)
        }
    }
}