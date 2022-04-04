package victor.training.reactive.usecase.zipchain;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static victor.training.reactive.Utils.sleep;

public class MultipleSubscribersMono_Zip3 {

   public static void main(String[] args) {
      C c = new MultipleSubscribersMono_Zip3().retrieveC(1L).blockOptional().orElseThrow();
      System.out.println("Got C = " + c);
   }

   // from id ==> get an A
   // from A ==> get a B
   // from A and B ==> get a C
   public Mono<C> retrieveC(long id) {
      return Mono.empty(); // TODO implement
   }
   // Points to make:
   // 1) zip + resubscribe
   // 2) cache
   // 3) zipWhen + TupleUtils


}
@Slf4j
class Apis {
   public Mono<Void> callMe(C c) {
      System.out.println("Got C = " + Objects.requireNonNull(c));
      return Mono.empty();
   }

   public static Mono<A> getA(long id) {
      return Mono.fromCallable(() -> {
         log.info("getA() -- Sending expensive REST call...");
         sleep(1000);
         return new A();
      });
   }

   public static Mono<B> getB(A a) {
      return Mono.fromCallable(()-> {
         log.info("getB({})", a);
         return new B();
      });
   }

   public static Mono<C> getC(A a, B b) {
      return Mono.fromCallable(() -> {
         log.info("getC({},{})", a, b);
         return new C();
      });
   }
}

class A {
}

class B {
}

class C {
}