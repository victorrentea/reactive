package victor.training.reactive.assignment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Customer {
   private Integer id;
   private boolean active;
   private boolean external;

   public Customer(Integer id, boolean active) {
      this.id = id;
      this.active = active;
   }
}
