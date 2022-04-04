package victor.training.reactive.reactor.complex;

import lombok.Data;

@Data
public class ProductDetailsResponse {
   private Long id;
   private String name;
   private boolean active;
   private boolean resealed;


   public Product toEntity() {
      return new Product(id,name,active,resealed, null);
   }
}
