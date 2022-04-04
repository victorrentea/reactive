package victor.training.reactive.usecase.grouping;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static java.time.Duration.ofMillis;

@Slf4j
public class GroupingFluxes {
   public static void main(String[] args) {
      //Depending on the message type, run one of the following flows:
      //TYPE1: Do nothing (ignore the message)
      //TYPE2: Call apiA(id) and apiB(id) in parallel
      //TYPE3: Call apiC(idList), buffering together requests such that
      //you send max 3 IDs, an ID waits max 500 millis

      Flux<Integer> messageStream =
          Flux.range(0, 10);
//          Flux.interval(ofMillis(200)).map(Long::intValue).take(10);

      new GroupingFluxes(new Apis()).processMessageStream(messageStream).block();
   }

   private final Apis apis;
   public GroupingFluxes(Apis apis) {
      this.apis = apis;
   }

   public Mono<Void> processMessageStream(Flux<Integer> messageStream) {
      return messageStream
//          .then() // starting point

          // solution:
          .groupBy(MessageType::forMessage)
          .flatMap(groupedFlux -> {
             switch (groupedFlux.key()) {
                case TYPE1_NEGATIVE:
                   return Mono.empty();
                case TYPE2_ODD:
                   return groupedFlux.flatMap(odd -> Mono.zip(apis.apiA(odd), apis.apiB(odd)));
                case TYPE3_EVEN:
                   return groupedFlux
                       .bufferTimeout(3, ofMillis(500))
                       .flatMap(evenPage -> apis.apiC(evenPage));
                default:
                   return Flux.error(new IllegalStateException("Unexpected value: " + groupedFlux.key()));
             }
          }).then();
   }

}

// key points:
// - switch{ default: throw } is illegal in a lambda returning Publisher<>
// - bufferTimeout
// - tests: testedMono.block() for simple cases
// - tests: PublisherProbe.subscriberCount
// - tests: StepVerifier.withVirtualTime + .defer() in mocks to run with Reactor Context
// - tests: TestPublisher.next() vs .emit()