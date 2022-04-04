package victor.training.reactive.reactor.lite;

import victor.training.reactive.reactor.lite.domain.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Part05Merge {

//========================================================================================

	// TODO Merge flux1 and flux2 values with interleave
	public Flux<User> mergeFluxWithInterleave(Flux<User> flux1, Flux<User> flux2) {
		return null;
	}

//========================================================================================

	// TODO Merge flux1 and flux2 values with no interleave (flux1 values and then flux2 values)
	public Flux<User> mergeFluxWithNoInterleave(Flux<User> flux1, Flux<User> flux2) {
		return null;
	}

//========================================================================================

	// TODO Create a Flux containing the value of mono1 then the value of mono2
	public Flux<User> createFluxFromMultipleMono(Mono<User> mono1, Mono<User> mono2) {
		return null;
	}

}
