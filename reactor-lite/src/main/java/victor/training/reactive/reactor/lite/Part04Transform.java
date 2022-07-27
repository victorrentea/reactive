package victor.training.reactive.reactor.lite;

import victor.training.reactive.reactor.lite.domain.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.stream.Stream;

public class Part04Transform {

//========================================================================================

	// TODO Capitalize the user username, firstname and lastname
	public Mono<User> capitalizeOne(Mono<User> mono) {
		return mono.map(User::capitalize);
	}

	//========================================================================================

	// TODO Capitalize the users username, firstName and lastName
	public Flux<User> capitalizeMany(Flux<User> flux) {
		return flux.map(User::capitalize);
	}

//========================================================================================

	// TODO Capitalize the users username, firstName and lastName using #asyncCapitalizeUser
	public Flux<User> asyncCapitalizeMany(Flux<User> flux) {
		// Functori si Monade: WTF ?!!?
//		Stream<Stream<String>> -> flatMap
//		Optional<Optional<String>> --> flatMap
//		Flux<Mono> --> flatMap --> Flux
//		Mono<Mono> --> flatMap --> Mono
//		Flux<Flux> --> flatMap --> Flux
//		Publisher<Publisher>

		return flux.flatMap(this::asyncCapitalizeUser);
	}



	protected Mono<User> asyncCapitalizeUser(User u) {
		return Mono.just(new User(u.getUsername().toUpperCase(), u.getFirstname().toUpperCase(), u.getLastname().toUpperCase()));
	}

}
