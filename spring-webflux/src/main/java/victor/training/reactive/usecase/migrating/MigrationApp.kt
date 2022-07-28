package victor.training.reactive.usecase.migrating

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.util.*

@RestController
@SpringBootApplication
class MigrationApp {
    @GetMapping
    fun reactiveEndpoint(@RequestParam(defaultValue = "1") id: String?): Mono<String> {
        return WebClient.create().get().uri("localhost:8080/call").retrieve()
            .bodyToMono(String::class.java) // on netty scheduler
            // .delayElement(Duration.ofMillis(1)) // moves on parallel
            .doOnNext { response: String? -> SomeLibrary.validate(response) }
            .map { obj: String -> obj.uppercase(Locale.getDefault()) }
    }

    @GetMapping
    fun call(): Mono<String> {
        return Mono.just("data")
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
//      Utils.installBlockHound();
            SpringApplication.run(MigrationApp::class.java, *args)
        }
    }
}