package victor.training.reactive.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
public class Event {
   enum Level {
      INFO, WARN, DEBUG
   }

   @Id
   private String id;
   private String value;
   private Level level;

   public Event(String value) {
      this.value = value;
   }
}
