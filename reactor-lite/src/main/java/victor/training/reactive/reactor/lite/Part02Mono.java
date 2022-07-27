package victor.training.reactive.reactor.lite;

import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;

public class Part02Mono {

//========================================================================================

	// TODO Return an empty Mono
	public Mono<String> emptyMono() {
		return Mono.empty();
	}

//========================================================================================

	// TODO Return a Mono that never emits any signal
	public Mono<String> monoWithNoSignal() {
		return apiNaspaVechi()
				.timeout(Duration.ofSeconds(5))
				;
	}

	@NotNull
	private static Mono<String> apiNaspaVechi() {
		return Mono.never();
//		return Mono.create(sink ->
//				{
//					// nimic
//				}
//		);
	}

//========================================================================================

	// TODO Return a Mono that contains a "foo" value
	public Mono<String> fooMono() {
		return Mono.just("foo");
	}

//========================================================================================

	// TODO Return a Mono of data. data can come null.
	public Mono<String> optionalMono(String data) {
//		return Mono.justOrEmpty(Optional.ofNullable(data));
		return Mono.justOrEmpty(data);
	}

//========================================================================================

	// TODO Create a Mono that emits an IllegalStateException
	public Mono<String> errorMono() {
		return Mono.error(new IllegalStateException());
	}

}
