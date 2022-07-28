//package victor.training.reactive.usecase.complex
//
//import org.slf4j.LoggerFactory
//import org.springframework.http.client.reactive.ClientHttpConnector
//import org.springframework.stereotype.Component
//import org.springframework.test.web.reactive.server.WebTestClient
//import org.springframework.test.web.reactive.server.WebTestClientConfigurer
//import org.springframework.web.reactive.function.client.ExchangeFilterFunction
//import org.springframework.web.reactive.function.client.WebClient
//import org.springframework.web.server.adapter.WebHttpHandlerBuilder
//@Component
//class MyWebTestClientConfigurer : WebTestClientConfigurer {
//    private val log = LoggerFactory.getLogger(MyWebTestClientConfigurer::class.java)
//    override fun afterConfigurerAdded(
//        builder: WebTestClient.Builder,
//        httpHandlerBuilder: WebHttpHandlerBuilder?,
//        connector: ClientHttpConnector?
//    ) {
//        log.info("COnfigure WebTestClient")
//        builder.filter { request, next ->
//           log.info("filter WebTestClient")
//
//           next.exchange(request)
//               .contextWrite { it.put("baseUrl", "fromTest") }
//       }
//    }
//
//}
//fun x() {
//WebTestClient.builder().
//
//}
