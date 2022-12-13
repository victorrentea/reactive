package victor.training.reactive.rabbit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExHandler {
  @ExceptionHandler
  public String method(Exception e) {
    log.error("VALEU", e);
    return "Totu bine, da mai fa un retry- mai baga o fisa";
  }
}
