package victor.training.reactive.mongo;

import org.springframework.data.mongodb.repository.Tailable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface EventReactiveRepo extends ReactiveCrudRepository<Event, Long> {
   @Tailable  //infinite flux bringing new events as they are inserted in the collection
   Flux<Event> findAllByIdNotNull();
}
