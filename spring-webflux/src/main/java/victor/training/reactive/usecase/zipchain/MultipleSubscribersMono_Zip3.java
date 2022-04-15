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
      return Mono.empty();
   }
}




class A {
}

class B {
}

class C {
}