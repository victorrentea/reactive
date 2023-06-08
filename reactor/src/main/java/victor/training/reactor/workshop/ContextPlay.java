package victor.training.reactor.workshop;

import reactor.core.publisher.Mono;

public class ContextPlay {
  public static void main(String[] args) {

    Mono.deferContextual(contextView -> {
          System.out.println("hocus pocus:" + contextView.get("user"));
          return Mono.empty();
        })
//        .map()
//        .flatMap()
//        .delayUntil()
        // ---- mai jos e spring
        .contextWrite(map -> map.put("user","jdoe"))
        .block();
  }
}
