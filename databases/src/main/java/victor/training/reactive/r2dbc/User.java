package victor.training.reactive.r2dbc;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("users")
@Data
public class User {
   @Id
   private Integer id;
   private String name;

   public User(String name) {
      this.name = name;
   }
}