package victor.training.reactive.reactor.lite;

import victor.training.reactive.reactor.lite.domain.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Part04Transform {

//========================================================================================

	// TODO Capitalize the user username, firstname and lastname
	public Mono<User> capitalizeOne(Mono<User> mono) {
		return null;
	}

//========================================================================================

	// TODO Capitalize the users username, firstName and lastName
	public Flux<User> capitalizeMany(Flux<User> flux) {
		return null;
	}

//========================================================================================

	// TODO Capitalize the users username, firstName and lastName using #asyncCapitalizeUser
	public Flux<User> asyncCapitalizeMany(Flux<User> flux) {
		return null;
	}

	protected Mono<User> asyncCapitalizeUser(User u) {
		return Mono.just(new User(u.getUsername().toUpperCase(), u.getFirstname().toUpperCase(), u.getLastname().toUpperCase()));
	}

}
