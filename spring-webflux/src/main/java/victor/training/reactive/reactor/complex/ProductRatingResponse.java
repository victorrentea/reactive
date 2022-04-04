package victor.training.reactive.reactor.complex;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRatingResponse {
   public static final ProductRatingResponse NO_RATING = new ProductRatingResponse();
   private int rating;
}

