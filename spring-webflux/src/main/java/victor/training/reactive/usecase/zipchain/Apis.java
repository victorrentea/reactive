package victor.training.reactive.usecase.zipchain;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static victor.training.reactive.Utils.sleep;

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