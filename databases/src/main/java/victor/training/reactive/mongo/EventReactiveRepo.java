package victor.training.reactive.mongo;

import org.springframework.data.mongodb.repository.Tailable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface EventReactiveRepo extends ReactiveCrudRepository<Event, Long> {
   @Tailable // tailable query e un select in mongo care NU SE TERMINA NICIODATA ci-ti impinge date pe Flux pe masura ce vin in DB
   Flux<Event> findAllByIdNotNull();
}
