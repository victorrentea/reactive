package victor.training.reactive.mongo;

import org.springframework.data.repository.CrudRepository;

public interface EventBlockingRepo extends CrudRepository<Event, Long> {
}
