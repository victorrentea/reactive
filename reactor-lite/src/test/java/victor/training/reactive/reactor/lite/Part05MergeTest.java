package victor.training.reactive.reactor.lite;

import victor.training.reactive.reactor.lite.domain.User;
import victor.training.reactive.reactor.lite.repository.ReactiveRepository;
import victor.training.reactive.reactor.lite.repository.ReactiveUserRepository;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import victor.training.reactive.reactor.lite.solved.Part05MergeSolved;

public class Part05MergeTest {

	Part05Merge workshop = new Part05Merge();
//	Part05Merge workshop = new Part05MergeSolved();

	final static User MARIE = new User("mschrader", "Marie", "Schrader");
	final static User MIKE = new User("mehrmantraut", "Mike", "Ehrmantraut");

	ReactiveRepository<User> repositoryWithDelay = new ReactiveUserRepository(500);
	ReactiveRepository<User> repository          = new ReactiveUserRepository(MARIE, MIKE);

//========================================================================================

	@Test
	public void mergeFluxWithInterleave() {
		Flux<User> flux = workshop.mergeFluxWithInterleave(repositoryWithDelay.findAll(), repository.findAll());
		StepVerifier.create(flux)
				.expectNext(MARIE, MIKE, User.SKYLER, User.JESSE, User.WALTER, User.SAUL)
				.verifyComplete();
	}

//========================================================================================

	@Test
	public void mergeFluxWithNoInterleave() {
		Flux<User> flux = workshop.mergeFluxWithNoInterleave(repositoryWithDelay.findAll(), repository.findAll());
		StepVerifier.create(flux)
				.expectNext(User.SKYLER, User.JESSE, User.WALTER, User.SAUL, MARIE, MIKE)
				.verifyComplete();
	}

//========================================================================================

	@Test
	public void createFluxFromMultipleMono() {
		Mono<User> skylerMono = repositoryWithDelay.findFirst();
		Mono<User> marieMono = repository.findFirst();
		Flux<User> flux = workshop.createFluxFromMultipleMono(skylerMono, marieMono);
		StepVerifier.create(flux)
				.expectNext(User.SKYLER, MARIE)
				.verifyComplete();
	}

}
