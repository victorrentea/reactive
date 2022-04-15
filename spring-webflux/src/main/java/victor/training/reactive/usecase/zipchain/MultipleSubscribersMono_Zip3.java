package victor.training.reactive.usecase.zipchain;

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
      Mono<A> monoA = Apis.getA(id).cache();

      Mono<B> monoB = monoA.flatMap(a -> Apis.getB(a));

      return monoA.zipWith(monoB, (a,b)->Apis.getC(a, b))
          .flatMap(monoC -> monoC);


//      return Apis.getA(id)
//          .flatMap(a -> Apis.getB(a).flatMap(b -> Apis.getC(a,b)) );

//      return Apis.getA(id)
//          .flatMap(a -> Apis.getB(a).map(b -> Tuples.of(a, b)))
//          .flatMap(tuple -> Apis.getC(tuple.getT1(), tuple.getT2()));
   }
}




class A {
}

class B {
}

class C {
}