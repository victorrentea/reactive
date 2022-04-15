package victor.training.reactive.usecase.monitoringinfinite;

import reactor.core.publisher.Mono;

class OrderApi {
   static Mono<Boolean> isOrderPresent(Long id) {
      return Mono.deferContextual(context ->
                   Mono.fromSupplier(() -> {
                      double r = Math.random();
                      if (r < 0.1) {
                         System.err.println("BUM NOW");
                         throw new RuntimeException("BUM");
                      }
                      return r < .5;
                   })
                   .doOnNext(b -> System.out.println("emiting user: "+context.get("username")+" " + b))
              );


   }
}