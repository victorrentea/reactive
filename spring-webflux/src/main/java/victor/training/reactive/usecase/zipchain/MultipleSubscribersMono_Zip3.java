package victor.training.reactive.usecase.zipchain;

import lombok.Value;
import reactor.core.publisher.Mono;

public class MultipleSubscribersMono_Zip3 {

   public static void main(String[] args) {
      C c = new MultipleSubscribersMono_Zip3().retrieveC(1L).blockOptional().orElseThrow();
      System.out.println("Got C = " + c);
   }

//   private static record MyData(Long customerId) {}
   // from id ==> get an A
   // from A ==> get a B
   // from A and B ==> get a C
   public Mono<C> retrieveC(long id) {

      // easiest
//      return  Apis.getA(id)
//          .flatMap(a -> Apis.getB(a).flatMap(b -> Apis.getC(a, b)));


//      Tuple4<Tuple2<CustomerId,Long>, Long,String, Map<String,Long>> tupleLove;

      // the mostscalable
//      return  Apis.getA(id)
//          .flatMap(a -> Apis.getB(a).map(b-> Tuples.of(a,b)))
//          .flatMap(TupleUtils.function((a, b) -> Apis.getC(a, b)));

      // the most magic
//      Mono<A> monoA = Apis.getA(id).cache(); // NOT a cross-request cache. \
//      // it just means that after a first data is emitted by the mono,
//      // any later .subscribe signal will be  immediately emitted the same value.
//      // "a cache inside the monoA variable"
//
//      Mono<B> monoB = monoA.flatMap(a -> Apis.getB(a));
//      return Mono.zip(monoA, monoB, (a,b) -> Apis.getC(a,b))
// ///      return monoA.zipWith(monoB, (a,b) -> Apis.getC(a,b))
//          .flatMap(m -> m);

//      A a = await getA();
//      B b = await getB(a);
//      C c = await getC(a,b);

      return Apis.getA(id)
          .zipWhen(Apis::getB, Apis::getC)
          .flatMap(m -> m);
   }
   // Points to make:
   // 1) zip + resubscribe ! > don't keep Mono in a F
   // 2) cache
   // 3) zipWhen + TupleUtils
}
//type CustomerId extends Long
@Value
class CustomerId { // microtypes to avoid confusion about what a Long means in a large Tuple
   long id;
}




class A {
}

class B {
}

class C {
}