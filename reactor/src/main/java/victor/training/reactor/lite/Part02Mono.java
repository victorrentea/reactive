package victor.training.reactor.lite;

import reactor.core.publisher.Mono;

public class Part02Mono {

//========================================================================================

	// TODO Return an empty Mono
	public Mono<String> emptyMono() {
		return null;
	}

//========================================================================================

	// TODO Return a Mono that never emits any signal
	public Mono<String> monoWithNoSignal() {
		return null;
	}

//========================================================================================

	// TODO Return a Mono that emits a "foo" value
	public Mono<String> fooMono() {
		return null;
	}

//========================================================================================

	// TODO Return a Mono of data passed as parameter. NOTE: data can come null.
	public Mono<String> optionalMono(String data) {
		return null;
	}

//========================================================================================

	// TODO Create a Mono that completes with an error signal of IllegalStateException
	public Mono<String> errorMono() {
		return null;
	}

}
