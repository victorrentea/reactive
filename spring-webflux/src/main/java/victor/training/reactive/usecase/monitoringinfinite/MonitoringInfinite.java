package victor.training.reactive.usecase.monitoringinfinite;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import victor.training.reactive.Utils;

import javax.annotation.PostConstruct;
import java.time.Duration;


@Service // imagine
public class MonitoringInfinite {

   public static void main(String[] args) {
      // Hooks.onOperatorDebug();

      monitor(Flux.interval(Duration.ofMillis(10)));
      Utils.sleep(1000000);
   }


   //You are monitoring an infinite flux of order ids (think Kafka stream)
   //Each id is checked in the OrderApi.isOrderPresent(id):Mono<Boolean>
   //If it is NOT found, or an error occurs, the id is sent to AuditApi.
   //  (In case of error, the AuditApi is retried once.)
   @PostConstruct
   private static void monitor(Flux<Long> orderIdInfiniteStream) {
      orderIdInfiniteStream
          .doOnNext(id -> System.out.println("I see ID " + id))

          .filterWhen(orderId -> OrderApi.isOrderPresent(orderId).map(b->!b))

//          .flatMap(orderId -> orderApi.isOrderPresent(orderId)
//              .map(isPresent -> Tuples.of(orderId, isPresent)))
//          .filter(Tuple2::getT2)
//          .map(Tuple2::getT1)


          .log("BEFORE FLATMAP")
          .flatMap(id -> AuditApi.auditOrderNotFound(id)
                  .retry(1)
//                .onErrorResume(tt -> Mono.empty())
          )
          .log("AFTER FLATMAP")
          .onErrorContinue((e, v) ->
              System.err.println("Magically caught an error from upstream, but allowed the flow to continue (no cancel) : " +
                                 e))
          .contextWrite(context -> {
             return context.put("username", "john"); // SecurityContextHolder
          })
          .subscribe();

   }
}
// checklist:
// - filterWhen
// - log() around audit sees CANCEL signal
// - onErrorResume BEFORE you reach the surface (top level reactive flow)
// - onErrorContinue propagates via Reactor Context
// - Hooks.onOperatorDebug() - global debug for exceptions