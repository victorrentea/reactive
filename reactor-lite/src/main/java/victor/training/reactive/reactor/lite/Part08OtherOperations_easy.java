package victor.training.reactive.reactor.lite;

import victor.training.reactive.reactor.lite.domain.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class Part08OtherOperations_easy {

//========================================================================================

	// TODO Create a Flux of user from Flux of username, firstname and lastname.
	public Flux<User> userFluxFromStringFlux(Flux<String> usernameFlux, Flux<String> firstnameFlux, Flux<String> lastnameFlux) {
		return null;
	}

//========================================================================================

	// TODO Return the mono which returns its value faster
	public Mono<User> useFastestMono(Mono<User> mono1, Mono<User> mono2) {
		return null;
	}

//========================================================================================

	// TODO Return the flux which returns the first value faster
	public Flux<User> useFastestFlux(Flux<User> flux1, Flux<User> flux2) {
		return null;
	}

//========================================================================================

	// TODO Convert the input Flux<User> to a Mono<Void> that represents the complete signal of the flux
	public Mono<Void> fluxCompletion(Flux<User> flux) {
		return null;
	}

//========================================================================================

	// TODO Return a valid Mono of user for null input and non null input user (hint: Reactive Streams do not accept null values)
	public Mono<User> nullAwareUserToMono(User user) {
		return null;
	}

//========================================================================================

	// TODO Return the same mono passed as input parameter, expect that it will emit User.SKYLER when empty
	public Mono<User> emptyToSkyler(Mono<User> mono) {
		return null;
	}

//========================================================================================

	// TODO Convert the input Flux<User> to a Mono<List<User>> containing list of collected flux values
	public Mono<List<User>> fluxCollection(Flux<User> flux) {
		return null;
	}

}
