package victor.training.reactive.usecase.complex;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private Long id;
    private String name;
    private boolean active;
    private boolean resealed;
    @With
    private ProductRatingResponse rating;

    //   public Product withRating(ProductRatingResponse newRating)  {
    //      return new Product(id, name, active, resealed, newRating);
    //   }

    public String toString() {
        return "Product(id=" + this.getId() + ", name=" + this.getName() + ", active=" + this.isActive() + ", resealed=" + this.isResealed() + ", rating=" + this.getRating() + ")";
    }
}
