package victor.training.reactive.reactor.lite;

import victor.training.reactive.reactor.lite.domain.User;
import victor.training.reactive.reactor.lite.repository.ReactiveRepository;
import victor.training.reactive.reactor.lite.repository.ReactiveUserRepository;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

public class Part06Request {

	public ReactiveRepository<User> repository = new ReactiveUserRepository();

//========================================================================================

	// TODO Create a StepVerifier that initially requests all values and expect 4 values to be received then a COMPLETION
	public StepVerifier requestAllExpectFourThenComplete(Flux<User> flux) {
		return null;
	}

//========================================================================================

	// TODO [HARD] Create a StepVerifier that initially requests all values and expect 3 values to be received
	// Note: don't wait for COMPLETION (as a matter of fact there could be 4 elements emitted).
	public StepVerifier requestExpectThreeOrMore(Flux<User> flux) {
		return null;
	}

//========================================================================================
	// TODO Return a Flux with all users stored in the repository that prints "Starring:" on subscribe, "firstname lastname" for all values and "The end!" on complete
	public Flux<User> fluxWithDoOnPrintln() {
		return null;
	}

//========================================================================================

	// TODO Return a Flux with all users stored in the repository that prints automatically logs for all Reactive Streams signals
	public Flux<User> fluxWithLog() {
		return null;
	}

//========================================================================================

	// TODO Create a StepVerifier that initially requests 1 value and expects User.SKYLER then requests another value and
	//  expects User.JESSE then stops verifying by cancelling the source
	// HARD: use .log() to check that you only request 1 element at a time (ie. no request(unbounded) is printed)
	public StepVerifier requestOne_oneMore_thenCancel(Flux<User> flux) {
		return null;
	}

//========================================================================================

	// TODO no matter how many items are requested to the flux you return, the upstream should be requested at most 10 at once
	public Flux<Integer> throttleUpstreamRequest(Flux<Integer> upstream) {
		return upstream;
	}

}
