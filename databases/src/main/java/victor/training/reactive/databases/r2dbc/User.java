package victor.training.reactive.databases.r2dbc;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

@Table("users")
@Data
public class User {
   @Id
   private Integer id;
   private String name;
//   @Version
//private Long version;
   public User(String name) {
      this.name = name;
   }
}