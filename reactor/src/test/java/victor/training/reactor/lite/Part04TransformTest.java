package victor.training.reactor.lite;

import victor.training.reactor.lite.domain.User;
import victor.training.reactor.lite.repository.ReactiveRepository;
import victor.training.reactor.lite.repository.ReactiveUserRepository;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class Part04TransformTest {

	Part04Transform workshop = new Part04Transform();
//	Part04Transform workshop = new Part04TransformSolved();
	ReactiveRepository<User> repository = new ReactiveUserRepository();

//========================================================================================

	@Test
	public void capitalizeOne() {
		Mono<User> mono = repository.findFirst();
		StepVerifier.create(workshop.capitalizeOne(mono))
				.expectNext(new User("SWHITE", "SKYLER", "WHITE"))
				.verifyComplete();
	}

//========================================================================================

	@Test
	public void capitalizeMany() {
		Flux<User> flux = repository.findAll();
		StepVerifier.create(workshop.capitalizeMany(flux))
				.expectNext(
					new User("SWHITE", "SKYLER", "WHITE"),
					new User("JPINKMAN", "JESSE", "PINKMAN"),
					new User("WWHITE", "WALTER", "WHITE"),
					new User("SGOODMAN", "SAUL", "GOODMAN"))
				.verifyComplete();
	}

//========================================================================================

	@Test
	public void  asyncCapitalizeMany() {
		Flux<User> flux = repository.findAll();
		StepVerifier.create(workshop.asyncCapitalizeMany(flux))
				.expectNext(
					new User("SWHITE", "SKYLER", "WHITE"),
					new User("JPINKMAN", "JESSE", "PINKMAN"),
					new User("WWHITE", "WALTER", "WHITE"),
					new User("SGOODMAN", "SAUL", "GOODMAN"))
				.verifyComplete();
	}

}
