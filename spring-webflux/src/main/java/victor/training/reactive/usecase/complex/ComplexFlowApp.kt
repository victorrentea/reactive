package victor.training.reactive.usecase.complex

import io.micrometer.core.aop.TimedAspect
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.server.ServerRequest


@RestController
@SpringBootApplication
@EnableCaching
class ComplexFlowApp  {

    private val log = LoggerFactory.getLogger(ComplexFlowApp::class.java)

    // enables the use of @Timed (exposed via /actuator/prometheus)
    @Bean
    fun timedAspect(meterRegistry: MeterRegistry?): TimedAspect {
        return TimedAspect(meterRegistry)
    }



//    @Bean
//    fun alwaysParallelWebfluxFilter(): WebFilter {
//        // ⚠️ WARNING: use this only when exploring the non-block-ness of your code.
//        Utils.installBlockHound(
//            listOf(
//                Tuples.of("io.netty.resolver.HostsFileParser", "parse"),
//                Tuples.of("victor.training.reactive.reactor.complex.ComplexFlowMain", "executeAsNonBlocking")
//            )
//        )
//        return WebFilter { exchange, chain ->
//            Mono.defer { chain.filter(exchange) }
//                .subscribeOn(Schedulers.parallel())
//        }
//    }



    @Bean
    @Scope("prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
    fun webClient(@Value("baseUrl") baseUrlFromProperties:String): WebClient {
        val baseUrl = baseUrlFromTest.get() ?: baseUrlFromProperties
        log.debug("New client with url: $baseUrl")
        return WebClient.create()
    }
}
val baseUrlFromTest = ThreadLocal.withInitial<String> { null }
fun main() {
    SpringApplication.run(ComplexFlowApp::class.java)
}

@Component
class ErrorAttributes : DefaultErrorAttributes() {
    fun getErrorAttributes(request: ServerRequest?, includeStackTrace: Boolean): Map<String, Any> {
        val ex = getError(request)
        val attributes = LinkedHashMap<String, Any>()
        attributes["status"] = HttpStatus.BAD_REQUEST.value()
        attributes["message"] = "bad" + ex.message
        return attributes
    }
}