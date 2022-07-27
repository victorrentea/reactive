package victor.training.reactive.reactor.lite;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import victor.training.reactive.reactor.lite.solved.Part01FluxSolved;

import java.util.List;

import static java.lang.System.currentTimeMillis;
import static org.assertj.core.api.Assertions.*;

public class Part01FluxTest {

	Part01Flux workshop = new Part01Flux();
//	Part01Flux workshop = new Part01FluxSolved();


//========================================================================================

	@Test
	public void emptyFlux() {
		Flux<String> flux = workshop.emptyFlux();

		StepVerifier.create(flux)
				.verifyComplete();
	}

//========================================================================================

	@Test
	public void fooBarFluxFromValues() {
		Flux<String> flux = workshop.fooBarFluxFromValues();
		StepVerifier.create(flux)
				.expectNext("foo", "bar")
				.verifyComplete();
	}

//========================================================================================

	@Test
	public void fluxFromList() {
		Flux<String> flux = workshop.fluxFromList(List.of("foo", "bar"));
		StepVerifier.create(flux)
				.expectNext("foo", "bar")
				.verifyComplete();
	}

//========================================================================================

	@Test
	public void errorFlux() {
		Flux<String> flux = workshop.errorFlux();
		StepVerifier.create(flux)
				.verifyError(IllegalStateException.class);
	}

//========================================================================================

	@Test
	public void countEach100ms() {
		Flux<Long> flux = workshop.countEach100ms();
		long t0 = currentTimeMillis();
		StepVerifier.create(flux)
				.expectNext(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L)
				.verifyComplete();
		long t1 = currentTimeMillis();
		assertThat(t1 - t0)
			.describedAs("Should take approx 1 second")
			.isGreaterThan(900).isLessThan(1200);
	}

}
