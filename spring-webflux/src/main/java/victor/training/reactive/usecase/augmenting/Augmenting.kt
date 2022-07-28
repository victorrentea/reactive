package victor.training.reactive.usecase.augmenting

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface NameApi {
    fun fetchName(userId: Long?): Mono<String>
}

interface AgeApi {
    fun fetchAge(userId: Long?): Mono<Int>
}

interface UnderAgeApi {
    fun notifyUnderAge(name: String?): Mono<Void?>?
}

class Augmenting(private val nameApi: NameApi, private val ageApi: AgeApi, private val underAgeApi: UnderAgeApi) {
    fun augment(userIds: List<Long?>?): Flux<User> {
        val userId = -1L
        val nameMono = nameApi!!.fetchName(userId)
        val ageMono = ageApi!!.fetchAge(userId)
        val age = 15
        if (age < 18) underAgeApi!!.notifyUnderAge("name")
        return Flux.empty()
    }
}

class User(val id: Long, val name: String, val age: Int)