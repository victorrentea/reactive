/*
package victor.training.reactive.usecase.complex

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters


@SpringBootTest
@AutoConfigureWebTestClient
internal class ComplexControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun executeAsNonBlocking() {
        val response = webTestClient.get()
            .uri("/complex")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .returnResult(String::class.java)
            .responseBody
            .blockFirst()
        assertThat(response).matches("is mocked")
    }
}*/
