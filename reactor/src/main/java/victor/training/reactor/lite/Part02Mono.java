package victor.training.reactor.lite;

import reactor.core.publisher.Mono;

public class Part02Mono {

//========================================================================================

	// TODO Return an empty Mono
	public Mono<String> emptyMono() {
		return Mono.empty();
	}

//========================================================================================

	// TODO Return a Mono that never emits any signal
	public Mono<String> monoWithNoSignal() {
		return Mono.never(); // de folosit prin teste sa vezi ce s-ar intampla daca NU vin datele
	}

//========================================================================================

	// TODO Return a Mono that emits a "foo" value
	public Mono<String> fooMono() {
		return Mono.just("foo");
	}

//========================================================================================

	// TODO Return a Mono of data passed as parameter. NOTE: data can come null.
	public Mono<String> optionalMono(String data) {
//		return Mono.just(data); // Reactor (mono/flux) au alergie la null -> NPE imediat
		return Mono.justOrEmpty(data); // de ex cand faci repo.findById(
	}

//========================================================================================

	// TODO Create a Mono that completes with an error signal of IllegalStateException
	public Mono<String> errorMono() {
		return Mono.error(new IllegalStateException()); // de ex iti vine asa cand
		// faci un repo.findById(id); si DB e unreachable
	}

}
