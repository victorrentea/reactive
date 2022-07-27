package victor.training.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class BlockHoundTest {
   @Test
   void test() {
      BlockHound.install();

      Assertions.assertThatThrownBy(() ->
              Mono.delay(Duration.ofSeconds(1))
                  .doOnNext(it -> {
                     try {
                        Thread.sleep(10);
                     } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                     }
                  })
                  .block())
          .hasCauseInstanceOf(BlockingOperationError.class)
      ;

   }

}
