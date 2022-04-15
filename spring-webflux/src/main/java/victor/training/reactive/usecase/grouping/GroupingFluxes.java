package victor.training.reactive.usecase.grouping;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static java.time.Duration.ofMillis;

@Slf4j
public class GroupingFluxes {
   public static void main(String[] args) {

      Flux<Integer> messageStream =
//          Flux.range(0, 10);
          Flux.interval(ofMillis(200)).map(Long::intValue).take(10);

      new GroupingFluxes(new Apis()).processMessageStream(messageStream).block();
   }

   private final Apis apis;
   public GroupingFluxes(Apis apis) {
      this.apis = apis;
   }

   //You are processing an infinite stream of incoming messages (eg from Kafka)
   //Depending on the message type, run one of the following flows:
   //TYPE1: Do nothing (ignore the message)
   //TYPE2: Call apiA(id) and apiB(id) in parallel
   //TYPE3: Call apiC(idList), buffering together requests such that
   //you send max 3 IDs, and
   //an ID waits max 500 millis

   public Mono<Void> processMessageStream(Flux<Integer> infiniteMessageStream) {
//      int message = 7;
//
//      MessageType messageType = MessageType.forMessage(message);
//      if (messageType == MessageType.TYPE2_ODD)

      return infiniteMessageStream
          .filter(m -> MessageType.forMessage(m) == MessageType.TYPE2_ODD)
          .flatMap(m -> Mono.zip(apis.apiA(m), apis.apiB(m)).then())
          .then();
   }

}

// key points:
// - switch{ default: throw } is illegal in a lambda returning Publisher<>
// - bufferTimeout
// - tests: testedMono.block() for simple cases
// - tests: PublisherProbe.subscriberCount
// - tests: StepVerifier.withVirtualTime + .defer() in mocks to run with Reactor Context
// - tests: TestPublisher.next() vs .emit()