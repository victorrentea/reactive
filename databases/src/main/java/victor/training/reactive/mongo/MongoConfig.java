package victor.training.reactive.mongo;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

import jakarta.annotation.PostConstruct;

import static java.time.LocalDateTime.now;

@RequiredArgsConstructor
@Configuration
@EnableMongoRepositories
@EnableReactiveMongoRepositories
public class MongoConfig {

   private final MongoOperations db;
   private final EventReactiveRepo repo;

   @PostConstruct
   public void setupDb() {
      if (!db.collectionExists(Event.class)) {
         db.createCollection(Event.class, CollectionOptions.empty()
             .capped()
             .size(1024)
             .maxDocuments(5));
         System.out.println("CREATED");
      } else {
         System.out.println("Mongo collection alreay found !");
      }
      repo.deleteAll().block();
      repo.save(new Event("Startup at " + now())).block(); // OMG! :) but only on startup
   }
}
