package victor.training.reactive.usecase.complex;
;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
class ExceptionInterceptor {

  @ExceptionHandler(Throwable.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public String method(Throwable ex) {
    System.out.printf("#sieu");
    log.error(ex.getMessage(), ex);
    return "Oups: " + ex.getMessage();
  }
}