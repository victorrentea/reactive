package victor.training.reactive.viorel

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Mono
import java.util.*

class MyTest {
    @Test
    fun `my test`() {
        val mockRepo = mockk<MyRepo>()
        val myService = MyService(mockRepo)
        every { mockRepo.save(any()) } answers {Mono.just(firstArg())}

        Assertions.assertThat(myService.prod("Rara").block()).isEqualTo("Rara")
    }
}

class MyService(private val myRepo: MyRepo) {
    fun prod(name:String): Mono<String> {
        val doc = MyDoc(name=name)
        return myRepo.save(doc)
            .map { it.name }
    }
}

interface MyRepo : ReactiveMongoRepository<MyDoc, String> {

}
@Document
class MyDoc(val id:String= UUID.randomUUID().toString(),
    val name:String) {
}