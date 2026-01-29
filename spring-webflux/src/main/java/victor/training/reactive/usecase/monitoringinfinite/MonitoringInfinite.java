package victor.training.reactive.usecase.monitoringinfinite;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import victor.training.reactive.Utils;

import java.time.Duration;


@Service // imagine
@RequiredArgsConstructor
public class MonitoringInfinite {

   public static void main(String[] args) {
      // Hooks.onOperatorDebug();

      new MonitoringInfinite(new Apis()).monitor(Flux.interval(Duration.ofMillis(10)));
      Utils.sleep(1000000); // keep main() alive
   }

   private final Apis apis;
   //You are monitoring an infinite flux of order ids (think Kafka stream)
   //Each id is checked in the apis.isOrderPresent(id):Mono<Boolean>
   //If it is NOT found, or an error occurs, the id is sent to AuditApi.
   //  (In case of error, the AuditApi is retried once.)
   @PostConstruct
   private void monitor(Flux<Long> orderIdInfiniteStream) {

   }
}
// checklist:
// - filterWhen
// - log() around audit sees CANCEL signal
// - onErrorResume BEFORE you reach the surface (top level reactive flow)
// - onErrorContinue propagates via Reactor Context
// - Hooks.onOperatorDebug() - global debug for exceptions