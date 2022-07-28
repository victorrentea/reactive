package victor.training.reactive.usecase.debugging

import org.springframework.stereotype.Service

@Service
class SomeService {
    fun logic(integer: String): Int {
        return integer.toInt()
    }
}