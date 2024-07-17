package victor.training.reactor.lite;

import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

public class Part01Flux {

//========================================================================================
	// TODO Return an empty Flux
	public Flux<String> emptyFlux() {
		return Flux.empty();
	}

//========================================================================================

	// TODO Return a Flux that contains 2 values "foo" and "bar" without using an array or a collection
	public Flux<String> fooBarFluxFromValues() {
		return Flux.just("foo","bar");
	}

//========================================================================================

	// TODO Create a Flux from a List that contains 2 values "foo" and "bar"
	public Flux<String> fluxFromList(List<String> list) {
		return Flux.fromIterable(list);
	}

//========================================================================================

	// TODO Create a Flux that emits an IllegalStateException
	public Flux<String> errorFlux() {
		// veste buna: nu mai ai voie sa faci throw vreodata!
		//  daca metoda intoarce Flux/Mono
		// Corolar: nici try{ pe partea cealalta
		return Flux.<String>error(new IllegalStateException()); // ~ throw
//				.onErrorReturn("Bere fara alcool"); // ~ catch

		// Doamne-fereste:
//		try { Flux<> f=metoda(); }catch(SuntBouRuntimeException wellcomeToReactiveHell) {}
	}

//========================================================================================

	// TODO Create a Flux that emits increasing values from 0 to 9 every 100ms
	public Flux<Long> countEach100ms() {
		return Flux.interval(Duration.ofMillis(50))
				.filter(e->e%2==0)
				.map(e->e/2)
				.take(10)
				;
	}

}
