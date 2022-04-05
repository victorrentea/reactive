package victor.training.reactive.usecase.zipchain;

import reactor.core.publisher.Mono;

public class MultipleSubscribersMono_Zip3 {

   public static void main(String[] args) {
      C c = new MultipleSubscribersMono_Zip3().retrieveC(1L).blockOptional().orElseThrow();
      System.out.println("Got C = " + c);
   }

   // from id ==> get an A
   // from A ==> get a B
   // from A and B ==> get a C
   public Mono<C> retrieveC(long id) {
      Mono<A> monoA = Apis.getA(id);

//      Mono<C> monoC = monoA.flatMap(a -> Apis.getB(a).flatMap(b -> Apis.getC(a, b)));

      Mono<B> monoB = monoA.flatMap(a -> Apis.getB(a));
      return Mono.zip(monoA, monoB, (a,b) -> Apis.getC(a,b))
          .flatMap(m -> m);
   }
   // Points to make:
   // 1) zip + resubscribe
   // 2) cache
   // 3) zipWhen + TupleUtils


}


class A {
}

class B {
}

class C {
}