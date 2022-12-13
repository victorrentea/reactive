package victor.training.reactive.mongo;

import org.springframework.data.mongodb.repository.Tailable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface EventReactiveRepo extends ReactiveCrudRepository<Event, Long> {
   @Tailable // poti sa deschizi un query in mongo si sa-l lasi deschi. Mongo iti va impinge orice date
   // se potrivesc pe ce aveai tu in vizor !! baza ==> PUSH ==>  in Java
   Flux<Event> findAllByIdNotNull();
}
