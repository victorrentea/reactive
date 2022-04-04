package victor.training.reactive.reactor.lite;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;
import reactor.util.annotation.Nullable;
import victor.training.reactive.reactor.lite.domain.User;
import victor.training.reactive.reactor.lite.repository.ReactiveRepository;
import victor.training.reactive.reactor.lite.repository.ReactiveUserRepository;
import victor.training.reactive.reactor.lite.solved.Part06RequestSolved;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Part06RequestTest {

   Part06Request workshop = new Part06Request();
//   Part06Request workshop = new Part06RequestSolved();
   ReactiveRepository<User> repository = new ReactiveUserRepository();

   PrintStream originalConsole = System.out;

   @Nullable //null when not useful
   ByteArrayOutputStream logConsole;

   @AfterEach
   public void afterEach() {
      if (logConsole != null) {
         originalConsole.println(logConsole.toString());
         System.setOut(originalConsole);
         logConsole = null;
      }
   }

//========================================================================================

   @Test
   public void requestAllExpectFourThenComplete() {
      Flux<User> flux = repository.findAll();
      StepVerifier verifier = workshop.requestAllExpectFourThenComplete(flux);
      verifier.verify();
   }

//========================================================================================

   @Test
   public void requestAllExpectThree_when3Given() {
      Flux<User> flux = Flux.just(User.SKYLER, User.JESSE, User.WALTER);
      StepVerifier verifier = workshop.requestExpectThreeOrMore(flux);
      verifier.verify();
   }
   @Test
   public void requestAllExpectThree_when4Given() {
      Flux<User> flux = Flux.just(User.SKYLER, User.JESSE, User.WALTER, User.SAUL);
      StepVerifier verifier = workshop.requestExpectThreeOrMore(flux);
      verifier.verify();
   }

//========================================================================================

   @Test
   public void fluxWithLog() {
      logConsole = new ByteArrayOutputStream();
      System.setOut(new PrintStream(logConsole));

      Flux<User> flux = workshop.fluxWithLog();

      StepVerifier.create(flux, 0)
          .thenRequest(1)
          .expectNextMatches(u -> true)
          .thenRequest(1)
          .expectNextMatches(u -> true)
          .thenRequest(2)
          .expectNextMatches(u -> true)
          .expectNextMatches(u -> true)
          .verifyComplete();


      Assertions.assertThat(logConsole.toString())
          .contains("onSubscribe")
          .contains("onNext")
          .contains("request(1)")
          .contains("onComplete");
   }

//========================================================================================

   @Test
   public void fluxWithDoOnPrintln() {
      Flux<User> flux = workshop.fluxWithDoOnPrintln();

      //setting up the logConsole here should ensure we only capture console logs from the Flux
      logConsole = new ByteArrayOutputStream();
      System.setOut(new PrintStream(logConsole));

      StepVerifier.create(flux)
          .expectNextCount(4)
          .verifyComplete();

      assertThat(logConsole.toString())
          .isEqualToIgnoringNewLines("Starring:\n"
                     + "Skyler White\n"
                     + "Jesse Pinkman\n"
                     + "Walter White\n"
                     + "Saul Goodman\n"
                     + "The end!\n");
   }

//========================================================================================

   @Test
   public void requestOne_oneMore_thenCancel() {
      TestPublisher<User> publisher = TestPublisher.createCold();

      publisher.next(User.SKYLER);
      publisher.next(User.JESSE);
      publisher.next(User.WALTER);

      Flux<User> flux = publisher.flux().log();
      StepVerifier verifier = workshop.requestOne_oneMore_thenCancel(flux);
      verifier.verify();

      publisher.assertCancelled();
   }

   //========================================================================================

   @Test
   public void throttleUpstreamRequest() {
      TestPublisher<Integer> upstream = TestPublisher.create();
      Flux<Integer> throttled = workshop.throttleUpstreamRequest(upstream.flux());

      StepVerifier.create(throttled)
          .then(() -> {
             upstream.assertMaxRequested(10).assertMinRequested(10);
             for (int i = 0; i < 10; i++) {
               upstream.next(i);
             }
          })
          .expectNextCount(10)
          .then(() -> {
             // actual = 8 items, things are a bit more complicated; see low tide - high tide policy
             upstream.assertMaxRequested(10);
             upstream.emit(1, 2);
          })
          .expectNextCount(2)
          .verifyComplete();
   }
}
