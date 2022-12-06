package victor.training.reactive.usecase.monitoringinfinite;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.SecurityProperties.User;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import victor.training.reactive.Utils;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


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
   @PostConstruct
   private void monitor(Flux<Long> orderIdInfiniteStream) {

//      Flux<Boolean> booleanFlux = orderIdInfiniteStream
////              .flatMapSequential(id -> apis.isOrderPresent(id)) // reordoneaza rez dar bufferizeaza -mem
////              .concatMap(id -> apis.isOrderPresent(id)) // nu paralelizeaza -timp -mem
////              .flatMap(id -> apis.isOrderPresent(id)) // bad practice-> nu limiteaza nicicum nr de apeluri paralele
//
//              .buffer(1000)
////              .flatMap(id -> apis.isOrderPresent(id), 3) // max 2 in paralel - tot prost
//
//              ;
//      List<Integer> userIds = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
//      List<User> useri = new ArrayList<>();
//      for (Integer id : userIds) {
//         var user = rest.get(id); // platesti retea 10-20-5 ms x N elemente (1000)
//         useri.add(user);
//      }

   }
}
// checklist:
// - filterWhen
// - log() around audit sees CANCEL signal
// - onErrorResume BEFORE you reach the surface (top level reactive flow)
// - onErrorContinue propagates via Reactor Context
// - Hooks.onOperatorDebug() - global debug for exceptions