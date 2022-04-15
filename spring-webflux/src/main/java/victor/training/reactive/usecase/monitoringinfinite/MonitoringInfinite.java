package victor.training.reactive.usecase.monitoringinfinite;

import reactor.core.publisher.Flux;
import victor.training.reactive.Utils;

import javax.annotation.PostConstruct;
import java.time.Duration;


//@Service
public class MonitoringInfinite {

   public static void main(String[] args) {
      // Hooks.onOperatorDebug();

      monitor(Flux.interval(Duration.ofMillis(10)));
      Utils.sleep(1000000);
   }


   //You are monitoring an infinite flux of order ids (think Kafka stream)
   //Each id is checked in the OrderApi.isOrderPresent(orderId):Mono<Boolean>
   //If it is NOT found, or an error occurs, the id is sent to AuditApi.
   //  (In case of error, the AuditApi is retried once.)
   @PostConstruct
   private static void monitor(Flux<Long> orderIdInfiniteStream) {
      orderIdInfiniteStream
          .log()
          .filterWhen(orderId -> OrderApi.isOrderPresent(orderId)
              .onErrorReturn(false)
              .map(isPresent -> !isPresent))
          .doOnNext(x -> System.out.println("AFTER: " + x))
          .doOnError(System.out::println)
          .flatMap(orderId -> AuditApi.auditOrderNotFound(orderId).retry(1))

          .onErrorContinue((ex, data)-> System.err.println("Ignoring:" + ex)) // HOW THE HACK does this work
          // Reactor Context
          // migrate from blocking code to reactive. > threads/schedulers

          .subscribe();
   }
}
// checklist:
// - filterWhen
// - log() around audit sees CANCEL signal
// - onErrorResume BEFORE you reach the surface (top level reactive flow)
// - onErrorContinue propagates via Reactor Context
