package victor.training.reactor.workshop;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import victor.training.reactor.lite.Part01Flux;
import victor.training.reactor.lite.Part02Mono;

import java.time.Duration;
import java.util.List;

import static java.lang.System.currentTimeMillis;
import static org.assertj.core.api.Assertions.assertThat;

public class BasicsTest {

//	Basics workshop = new Basics();
	Basics workshop = new BasicsSolved();

	//========================================================================================

	@Test
	public void emptyMono() {
		Mono<String> mono = workshop.emptyMono();
		StepVerifier.create(mono)
						.verifyComplete();
	}

	//========================================================================================

	@Test
	public void monoWithNoSignal() {
		Mono<String> mono = workshop.monoWithNoSignal();
		StepVerifier
						.create(mono)
						.expectSubscription()
						.expectTimeout(Duration.ofSeconds(1))
						.verify();
	}

	//========================================================================================

	@Test
	public void fooMono() {
		Mono<String> mono = workshop.fooMono();
		StepVerifier.create(mono)
						.expectNext("foo")
						.verifyComplete();
	}

	//========================================================================================

	@Test
	public void optionalMono() {
		StepVerifier.create(workshop.optionalMono("foo"))
						.expectNext("foo")
						.verifyComplete();

		StepVerifier.create(workshop.optionalMono(null))
						.verifyComplete();
	}

	//========================================================================================

	@Test
	public void errorMono() {
		Mono<String> mono = workshop.errorMono();
		StepVerifier.create(mono)
						.verifyError(IllegalStateException.class);
	}

	@Test
	void delayedCompletion() {
		Mono<Void> mono = workshop.delayedCompletion();
		StepVerifier.create(mono)
						.expectSubscription()
						.expectNoEvent(Duration.ofMillis(50))
						.verifyComplete();
	}

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
