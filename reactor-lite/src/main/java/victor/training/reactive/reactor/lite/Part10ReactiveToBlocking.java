package victor.training.reactive.reactor.lite;

import victor.training.reactive.reactor.lite.domain.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class 	Part10ReactiveToBlocking {

//========================================================================================

	// TODO Return the user contained in that Mono
	public User monoToValue(Mono<User> mono) {
		return null;
	}

//========================================================================================

	// TODO Return the users contained in that Flux
	public Iterable<User> fluxToValues(Flux<User> flux) {
		return null;
	}

}
