package victor.training.reactive.reactor.lite;

import java.util.Iterator;

import victor.training.reactive.reactor.lite.domain.User;
import victor.training.reactive.reactor.lite.repository.BlockingUserRepository;
import victor.training.reactive.reactor.lite.repository.ReactiveRepository;
import victor.training.reactive.reactor.lite.repository.ReactiveUserRepository;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import victor.training.reactive.reactor.lite.solved.Part11BlockingToReactiveSolved;
import victor.training.util.NonBlocking;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Learn how to call blocking code from Reactive one with adapted concurrency strategy for
 * blocking code that produces or receives data.
 *
 * For those who know RxJava:
 *  - RxJava subscribeOn = Reactor subscribeOn
 *  - RxJava observeOn = Reactor publishOn
 *
 * @see Flux#subscribeOn(Scheduler)
 * @see Flux#publishOn(Scheduler)
 * @see Schedulers
 */
public class Part11BlockingToReactiveTest {

	Part11BlockingToReactive workshop = new Part11BlockingToReactive();
//	Part11BlockingToReactive workshop = new Part11BlockingToReactiveSolved();

//========================================================================================

	@Test
	@NonBlocking
	public void blockingRepositoryToFlux() {
		BlockingUserRepository repository = new BlockingUserRepository();
		Flux<User> flux = workshop.blockingRepositoryToFlux(repository);
		assertThat(repository.getCallCount()).isEqualTo(0).withFailMessage("The call to findAll must be deferred until the flux is subscribed");
		StepVerifier.create(flux)
				.expectNext(User.SKYLER, User.JESSE, User.WALTER, User.SAUL)
				.verifyComplete();
	}

//========================================================================================

	@Test
	@NonBlocking
	public void fluxToBlockingRepository_nonBlocking() {
		ReactiveRepository<User> reactiveRepository = new ReactiveUserRepository();
		BlockingUserRepository blockingRepository = new BlockingUserRepository(new User[]{});
		Mono<Void> complete = workshop.fluxToBlockingRepository(reactiveRepository.findAll(), blockingRepository);
		assertThat(blockingRepository.getCallCount()).isEqualTo(0);
		StepVerifier.create(complete)
				.verifyComplete();
	}
	@Test
	public void fluxToBlockingRepository_data() {
		ReactiveRepository<User> reactiveRepository = new ReactiveUserRepository();
		BlockingUserRepository blockingRepository = new BlockingUserRepository(new User[]{});

		workshop.fluxToBlockingRepository(reactiveRepository.findAll(), blockingRepository).block();

		Iterator<User> it = blockingRepository.findAll().iterator();
		assertThat(it.next()).isEqualTo(User.SKYLER);
		assertThat(it.next()).isEqualTo(User.JESSE);
		assertThat(it.next()).isEqualTo(User.WALTER);
		assertThat(it.next()).isEqualTo(User.SAUL);
		assertThat(it.hasNext()).isFalse();
	}

}
