package victor.training.reactive.reactor.complex;

import lombok.Value;
import lombok.With;

@Value
public class Product {
   Long id;
   String name;
   boolean active;
   boolean resealed;
   @With
   ProductRatingResponse rating;

//   public Product withRating(ProductRatingResponse newRating)  {
//      return new Product(id, name, active, resealed, newRating);
//   }

   public String toString() {
      return "Product(id=" + this.getId() + ", name=" + this.getName() + ", active=" + this.isActive() + ", resealed=" + this.isResealed() + ", rating=" + this.getRating() + ")";
   }
}
