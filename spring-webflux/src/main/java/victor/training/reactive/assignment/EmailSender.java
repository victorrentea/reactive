package victor.training.reactive.assignment;

import reactor.core.publisher.Mono;

public interface EmailSender {
   Mono<Void> sendInactiveCustomerEmail(Customer customer);
}
