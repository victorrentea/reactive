package victor.training.reactive.usecase.grouping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class GroupingFluxes {
   public static void main(String[] args) {
      Flux<Integer> messageStream =
          Flux.range(0, 10);
//          Flux.interval(ofMillis(200)).map(Long::intValue).take(10);

      new GroupingFluxes(new Apis()).processMessageStream(messageStream).block();
   }

   private final Apis apis;

//   @PostConstruct
//   public void atStartup() {
//      processMessageStream(mq).subscribe();
//   }


   public Mono<Void> processMessageStream(Flux<Integer> messageStream) {
      // mesaje de diferite tipuri venite de pe websocket

//      Integer message;
//      MessageType type = MessageType.forMessage(message);

      //Depending on the message type, run one of the following flows:
      //TYPE1: Do nothing (ignore the message), sau niste treaba in memorie (put ntr-un map)
      //TYPE2: Call apiA(message) and apiB(message) in parallel
      //TYPE3: Call apiC(List.of(message))
      // -- done
      //TYPE3(HARD): Call apiC(messageList), buffering together requests such that
      //HARD: send max 3 IDs, but an ID waits max 500 millis


      return messageStream
              .flatMap(m -> {
                 switch (MessageType.forMessage(m)){
                    case TYPE1_NEGATIVE:
                       System.out.println("ceva in memorie" + m);
                       return Mono.empty();
                    case TYPE2_ODD:
                       return Mono.zip(apis.apiA(m), apis.apiB(m));
                    case TYPE3_EVEN:
                       return apis.apiC(List.of(m));
                    default:
                       return Mono.error(new IllegalStateException("Unexpected value: " + MessageType.forMessage(m)));
                 }
              })
          .then()
          ;
   }

}

// key points:
// - Flux.groupBy
// - switch{ default: throw } is illegal in a lambda returning Publisher<>
// - bufferTimeout
// - tests: testedMono.block() for simple cases
// - tests: PublisherProbe.subscriberCount
// - tests: StepVerifier.withVirtualTime + .defer() in mocks to run with Reactor Context
// - tests: TestPublisher.next() vs .emit()



// SOLUTION:
// .groupBy(m -> MessageType.forMessage(m))
//          .flatMap(groupedFlux -> {
//             switch (groupedFlux.key()) {
//                case TYPE1_NEGATIVE:
//                   return Mono.empty();
//                case TYPE2_ODD:
//                   return groupedFlux
//                       .flatMap(m2 ->Mono.zip(apis.apiA(m2), apis.apiB(m2)))
//                       .then();
//                case TYPE3_EVEN:
//                   return groupedFlux
//                       .bufferTimeout(3, ofMillis(500))
//                       .flatMap(pageOfType3 -> apis.apiC(pageOfType3))
//                       .then();
//                default:
//                   return Flux.error(new IllegalStateException("Unexpected value: " + groupedFlux.key()));
//             }
//          })