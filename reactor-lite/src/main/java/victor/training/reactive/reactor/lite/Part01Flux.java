package victor.training.reactive.reactor.lite;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.swing.*;
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
//		Mono.fromSupplier(() -> soapCallSaFacClabuciTine1500ms()) // nu blocheaza, pentru ca nu cheama dusmanu inca
//		Mono.just(soapCallSaFacClabuciTine1500ms()) // blocheza threadul, vin la tine
		return Flux.just("foo", "bar");
//		return Flux.defer(() -> soapCallSaFacClabuciTine1500ms());
	}

//========================================================================================

	// TODO Create a Flux from a List that contains 2 values "foo" and "bar"
	public Flux<String> fluxFromList(List<String> list) {
//		return Flux.fromIterable(list);
//		JButton button;
		return Flux.create(sink -> {
//			button.addActionListener(e -> sink.next("click tata!"));
			sink.next("foo");
			sink.next("bar");
			sink.complete();
		});
	}

//========================================================================================

	// TODO Create a Flux that emits an IllegalStateException
	public Flux<String> errorFlux() {
//		return Flux.create(sink -> sink.error(new IllegalStateException()));
		return Flux.error(new IllegalStateException());
	}

//========================================================================================

	// TODO Create a Flux that emits increasing values from 0 to 9 each 100ms
	public Flux<Long> countEach100ms() {
		return Flux.range(0, 10)
				.delayElements(Duration.ofMillis(100))// TODO de explorat
				.map(Long::valueOf);
//		return Flux.interval(Duration.ofMillis(100))
//				.take(10)
//				;
	}

}
