/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package victor.training.reactive.reactor.lite;

import java.util.concurrent.CompletableFuture;

import victor.training.reactive.reactor.lite.domain.User;
import victor.training.reactive.reactor.lite.repository.ReactiveRepository;
import victor.training.reactive.reactor.lite.repository.ReactiveUserRepository;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import victor.training.reactive.reactor.lite.solved.Part09AdaptSolved;

public class Part09AdaptTest {

	Part09Adapt workshop = new Part09Adapt();
//	Part09Adapt workshop = new Part09AdaptSolved();
	ReactiveRepository<User> repository = new ReactiveUserRepository();

//========================================================================================

	@Test
	public void fromFluxToFlowable() {
		Flux<User> flux = repository.findAll();
		Flowable<User> flowable = workshop.fromFluxToFlowable(flux);
		StepVerifier.create(workshop.fromFlowableToFlux(flowable))
				.expectNext(User.SKYLER, User.JESSE, User.WALTER, User.SAUL)
				.verifyComplete();
	}

//========================================================================================

	@Test
	public void fromFluxToObservable() {
		Flux<User> flux = repository.findAll();
		Observable<User> observable = workshop.fromFluxToObservable(flux);
		StepVerifier.create(workshop.fromObservableToFlux(observable))
				.expectNext(User.SKYLER, User.JESSE, User.WALTER, User.SAUL)
				.verifyComplete();
	}

//========================================================================================

	@Test
	public void fromMonoToSingle() {
		Mono<User> mono = repository.findFirst();
		Single<User> single = workshop.fromMonoToSingle(mono);
		StepVerifier.create(workshop.fromSingleToMono(single))
				.expectNext(User.SKYLER)
				.verifyComplete();
	}

//========================================================================================

	@Test
	public void fromMonoToCompletableFuture() {
		Mono<User> mono = repository.findFirst();
		CompletableFuture<User> future = workshop.fromMonoToCompletableFuture(mono);
		StepVerifier.create(workshop.fromCompletableFutureToMono(future))
				.expectNext(User.SKYLER)
				.verifyComplete();
	}

}
