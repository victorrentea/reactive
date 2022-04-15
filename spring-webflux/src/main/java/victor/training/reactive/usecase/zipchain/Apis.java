package victor.training.reactive.usecase.zipchain;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import static victor.training.reactive.Utils.sleep;

@Slf4j
class Apis {
   public Mono<A> getA(long id) {
      return Mono.fromCallable(() -> {
         log.info("getA() -- Sending expensive REST call...");
         sleep(1000);
         return new A();
      });
   }

   public Mono<B> getB(A a) {
      return Mono.fromCallable(()-> {
         log.info("getB({})", a);
         return new B();
      });
   }

   public Mono<C> getC(A a, B b) {
      return Mono.fromCallable(() -> {
         log.info("getC({},{})", a, b);
         return new C();
      });
   }


   static class A {
   }

   static class B {
   }

   static class C {
   }
}