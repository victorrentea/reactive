package victor.training.reactive.reactor.lite;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import victor.training.reactive.intro.Utils;
import victor.training.reactive.reactor.lite.solved.Part12AdvancedSolved;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class Part12AdvancedTest {

   private Part12Advanced workshop = new Part12Advanced();
//   private Part12Advanced workshop = new Part12AdvancedSolved();

   @Test
   public void defer() {
      Flux<Integer> flux = workshop.defer();
      List<Integer> sequence1 = flux.collectList().block();
      List<Integer> sequence2 = flux.collectList().block();
      assertThat(sequence1).isNotEqualTo(sequence2);
   }


   @Test
   public void hotPublisher() throws InterruptedException {
      assertThat(workshop.hotPublisher().blockFirst()).isLessThan(2);

      Flux<Long> hot = workshop.hotPublisher();
      Thread.sleep(410);
      assertThat(hot.blockFirst()).isGreaterThan(3);
   }
   
   @Test
   public void replay() {
      String reactiveManifesto = "Large systems are composed of smaller ones and therefore " +
                                 "depend on the Reactive properties of their constituents.";
      Flux<String> timedFlux = workshop.replay(Flux.interval(Duration.ofMillis(10)), reactiveManifesto);
      System.out.println("0");

      assertThat(timedFlux.take(3).collectList().block()).containsExactly("Large", "systems", "are");
      System.out.println("1");

      Utils.sleep(60);
      assertThat(timedFlux.take(3).collectList().block()).containsExactly("Large", "systems", "are");
      System.out.println("2");
      assertThat(timedFlux.collectList().block()).containsExactly(reactiveManifesto.split("\\s"));
   }

   @Test
   public void share() {

      Supplier<String> supplier = mock(Supplier.class);
      Mono<String> mono = workshop.share(supplier);
      verifyNoInteractions(supplier);

      when(supplier.get()).thenReturn("Mama-Mia");

      String r1 = mono.block();
      String r2 = mono.block();

      verify(supplier, times(1)).get();

      assertThat(r1).startsWith("Mama-Mia");
      assertThat(r2).startsWith("Mama-Mia");
      assertThat(r1).isNotEqualTo(r2);

   }


   @Test
   public void reactorContext() {
      Mono<String> withContext = workshop.reactorContext()
          .contextWrite(context -> context.put("username", "John"));

      Duration duration = StepVerifier.create(withContext)
          .expectNext("Hello John")
          .verifyComplete();
      assertThat(duration).isGreaterThan(Duration.ofMillis(900));
   }

}
