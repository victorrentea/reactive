package victor.training.reactive.usecase.complex

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionInterceptor {
    private val log = LoggerFactory.getLogger(ExceptionInterceptor::class.java)

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun serverExceptionHandler(ex: Exception): String {
        log.error(ex.message, ex)
        return "Oups: " + ex.message
    }
}