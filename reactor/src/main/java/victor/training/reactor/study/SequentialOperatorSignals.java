package victor.training.reactor.study;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import victor.training.reactor.lite.Utils;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.IntStream;

import static reactor.core.scheduler.Schedulers.boundedElastic;
import static reactor.core.scheduler.Schedulers.parallel;

@Slf4j
public class SequentialOperatorSignals {
  static class State {
    Deque<String> elements = new ArrayDeque<>();
    Flux<String> process(String element) {
      log.info("Starting {}✅", element);
      Utils.sleep(10);
      elements.addLast(element);
      Flux<String> r;
      if (elements.size() > 2) {
        r = Flux.just(elements.removeFirst() + " " + elements.removeFirst() + " " + elements.removeFirst());
      } else {
        r = Flux.empty();
      }
      log.info("Finished {}❌", element);
      return r;
    }
  }

  public static void main(String[] args) {
    State state = new State();
    Flux.fromStream(IntStream.range(0, 50).mapToObj(Integer::toString))
        .flatMap(e->doStuff(e))
//        .concatMap(element -> state.process(element))

        .flatMap(element -> state.process(element))
        // 👍 Does NOT race as REACTOR guarantees that one operator's -> is never called in parallel on the same Flux instance
        .blockLast();
  }

  private static Mono<String> doStuff(String e) {
    if (Math.random()<0.3) {
      return Mono.just(e)
          .delayElement(Duration.ofMillis((long) (10 + Math.random() * 50)))
          .publishOn(parallel());
    } else {
      return Mono.just(e)
          .delayElement(Duration.ofMillis((long) (10 + Math.random() * 50)))
          .publishOn(boundedElastic());
    }
  }
}
