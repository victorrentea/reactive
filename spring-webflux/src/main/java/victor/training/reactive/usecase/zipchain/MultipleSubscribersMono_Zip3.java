package victor.training.reactive.usecase.zipchain;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import victor.training.reactive.usecase.zipchain.Apis.A;
import victor.training.reactive.usecase.zipchain.Apis.B;
import victor.training.reactive.usecase.zipchain.Apis.C;

@RequiredArgsConstructor
public class MultipleSubscribersMono_Zip3 {
   public static void main(String[] args) {
      C c = new MultipleSubscribersMono_Zip3(new Apis()).retrieveC(1L).blockOptional().orElseThrow();
      System.out.println("Got C = " + c);
   }

   private final Apis apis;
   // from id ==> get A
   // from A ==> get B
   // from A and B ==> get C
   public Mono<C> retrieveC(long id) {
      // TODO fix
      Mono<A> monoA = apis.getA(id);

      Mono<B> monoB = apis.getB(null);

      Mono<C> monoC = apis.getC(null, null);

      return monoC;
   }
}

// Points to make:
// - zip + resubscribe ! > don't keep Mono in a F
// - cache
// - zipWhen + TupleUtils!



// 1: nested flatMaps
// return  Apis.getA(id).flatMap(a -> Apis.getB(a).flatMap(b -> Apis.getC(a, b)));

// 2: accumulating Tuples
// return  Apis.getA(id)
//  .flatMap(a -> Apis.getB(a).map(b-> Tuples.of(a,b)))
//  .flatMap(TupleUtils.function((a, b) -> Apis.getC(a, b)));

// 3: Mono.cache()
// Mono<A> monoA = Apis.getA(id).cache(); // per-Mono instance cache - any second subscriber will be emitted the cached value.
// Mono<B> monoB = monoA.flatMap(a -> Apis.getB(a));
// return Mono.zip(monoA, monoB, (a,b) -> Apis.getC(a,b)).flatMap(m -> m);

// 4: Mono.zipWith()
// return Apis.getA(id)
//     .zipWhen(a -> Apis.getB(a), (a, b) -> Apis.getC(a, b))
//     .flatMap(identity());
