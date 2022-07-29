package victor.training.reactive.usecase.complex;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import victor.training.reactive.Utils;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRatingResponse {
   public static final ProductRatingResponse NO_RATING = new ProductRatingResponse();
   private int rating;

   public void method() {

//      Mono.fromRunnable(() -> gunoi())
//              .subscribeOn(Schedulers.boundedElastic())o
   }

   public void gunoi() {
      Utils.sleep(1100);
   }
}

