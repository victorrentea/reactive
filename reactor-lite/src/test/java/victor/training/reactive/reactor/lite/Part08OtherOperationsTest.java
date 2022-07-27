package victor.training.reactive.reactor.lite;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.PublisherProbe;
import victor.training.reactive.reactor.lite.domain.User;
import victor.training.reactive.reactor.lite.repository.ReactiveRepository;
import victor.training.reactive.reactor.lite.repository.ReactiveUserRepository;

import java.util.Arrays;
import java.util.List;

public class Part08OtherOperationsTest {

	Part08OtherOperations_easy workshop = new Part08OtherOperations_easy();
//	Part08OtherOperations_easy workshop = new Part08OtherOperationsSolved();

	final static User MARIE = new User("mschrader", "Marie", "Schrader");
	final static User MIKE = new User("mehrmantraut", "Mike", "Ehrmantraut");

//========================================================================================

	@Test
	public void userFluxFromStringFlux() {
		Flux<String> usernameFlux = Flux.just(User.SKYLER.getUsername(), User.JESSE.getUsername(), User.WALTER.getUsername(), User.SAUL.getUsername());
		Flux<String> firstnameFlux = Flux.just(User.SKYLER.getFirstname(), User.JESSE.getFirstname(), User.WALTER.getFirstname(), User.SAUL.getFirstname());
		Flux<String> lastnameFlux = Flux.just(User.SKYLER.getLastname(), User.JESSE.getLastname(), User.WALTER.getLastname(), User.SAUL.getLastname());
		Flux<User> userFlux = workshop.userFluxFromStringFlux(usernameFlux, firstnameFlux, lastnameFlux);
		StepVerifier.create(userFlux)
				.expectNext(User.SKYLER, User.JESSE, User.WALTER, User.SAUL)
				.verifyComplete();
	}

//========================================================================================

	@Test
	public void useFastestMono() {
		ReactiveRepository<User> repository = new ReactiveUserRepository(MARIE);
		ReactiveRepository<User> repositoryWithDelay = new ReactiveUserRepository(250, MIKE);
		Mono<User> mono = workshop.useFastestMono(repository.findFirst(), repositoryWithDelay.findFirst());
		StepVerifier.create(mono)
				.expectNext(MARIE)
				.verifyComplete();

		repository = new ReactiveUserRepository(250, MARIE);
		repositoryWithDelay = new ReactiveUserRepository(MIKE);
		mono = workshop.useFastestMono(repository.findFirst(), repositoryWithDelay.findFirst());
		StepVerifier.create(mono)
				.expectNext(MIKE)
				.verifyComplete();
	}

//========================================================================================

	@Test
	public void useFastestFlux() {
		ReactiveRepository<User> repository = new ReactiveUserRepository(MARIE, MIKE);
		ReactiveRepository<User> repositoryWithDelay = new ReactiveUserRepository(250);
		Flux<User> flux = workshop.useFastestFlux(repository.findAll(), repositoryWithDelay.findAll());
		StepVerifier.create(flux)
				.expectNext(MARIE, MIKE)
				.verifyComplete();

		repository = new ReactiveUserRepository(250, MARIE, MIKE);
		repositoryWithDelay = new ReactiveUserRepository();
		flux = workshop.useFastestFlux(repository.findAll(), repositoryWithDelay.findAll());
		StepVerifier.create(flux)
				.expectNext(User.SKYLER, User.JESSE, User.WALTER, User.SAUL)
				.verifyComplete();
	}

//========================================================================================

	@Test
	public void fluxCompletion() {
		ReactiveRepository<User> repository = new ReactiveUserRepository();
		PublisherProbe<User> probe = PublisherProbe.of(repository.findAll());
		Mono<Void> completion = workshop.fluxCompletion(probe.flux());
		StepVerifier.create(completion)
				.verifyComplete();
		probe.assertWasRequested();
	}

//========================================================================================

	@Test
	public void nullAwareUserToMono() {
		Mono<User> mono = workshop.nullAwareUserToMono(User.SKYLER);
		StepVerifier.create(mono)
				.expectNext(User.SKYLER)
				.verifyComplete();
		mono = workshop.nullAwareUserToMono(null);
		StepVerifier.create(mono)
				.verifyComplete();
	}

//========================================================================================

	@Test
	public void emptyToSkyler() {
		Mono<User> mono = workshop.emptyToSkyler(Mono.just(User.WALTER));
		StepVerifier.create(mono)
				.expectNext(User.WALTER)
				.verifyComplete();
		mono = workshop.emptyToSkyler(Mono.empty());
		StepVerifier.create(mono)
				.expectNext(User.SKYLER)
				.verifyComplete();
	}

//========================================================================================

	@Test
	public void fluxCollection() {
		ReactiveRepository<User> repository = new ReactiveUserRepository();
		Mono<List<User>> collection = workshop.fluxCollection(repository.findAll());
		StepVerifier.create(collection)
				.expectNext(Arrays.asList(User.SKYLER, User.JESSE, User.WALTER, User.SAUL))
				.verifyComplete();
	}

}
