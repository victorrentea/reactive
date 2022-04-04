package victor.training.reactive.assignment;

import reactor.core.publisher.Mono;

public interface CustomerReactiveRepo {
   Mono<Customer> findById(Integer id);
}
