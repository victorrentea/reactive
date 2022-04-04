package victor.training.reactive.databases.mongo;

import org.springframework.data.mongodb.repository.Tailable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface EventReactiveRepo extends ReactiveCrudRepository<Event, Long> {
   @Tailable
   Flux<Event> findAllByIdNotNull();
}
