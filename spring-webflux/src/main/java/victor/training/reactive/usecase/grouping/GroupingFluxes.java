package victor.training.reactive.usecase.grouping;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import victor.training.reactive.intro.Utils;

@Slf4j
public class GroupingFluxes {
   public static void main(String[] args) {
      //Depending on the message type, run one of the following flows:
      //TYPE1: Do nothing (ignore the message)
      //TYPE2: Call apiA(id) and apiB(id) in parallel
      //TYPE3: Call apiC(idList), buffering together requests such that
      //you send max 3 IDs, an ID waits max 500 millis

      Flux<Integer> messageStream = Flux.range(0, 100);

      new GroupingFluxes(new Apis()).processMessageStream(messageStream);

      Utils.waitForEnter();
   }

   private final Apis apis;
   public GroupingFluxes(Apis apis) {
      this.apis = apis;
   }

   public void processMessageStream(Flux<Integer> messageStream) {
      messageStream
          .flatMap(message -> {
             if (MessageType.forMessage(message) == MessageType.TYPE2_ODD) {
                return apis.apiA(message);
             } else {
                return Mono.empty();
             }
          })
          .subscribe();
   }

}

