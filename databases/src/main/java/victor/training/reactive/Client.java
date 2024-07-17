package victor.training.reactive;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@RestController
@RequiredArgsConstructor
public class Client {
  @GetMapping("/vali")
  public Mono<Void> trimitLaUnServerGrameziDeDate() {
//    List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    //10000 elemente
    List<Integer> numbers = IntStream.range(0, 100).boxed().collect(Collectors.toList());
    return Flux.fromIterable(numbers)
        .buffer(5)
        .flatMap(this::sendChunk)
        .then();
  }

  private Mono<Void> sendChunk(List<Integer> numbers) {
    return WebClient.create().post().uri("http://localhost:8080/process")
        .bodyValue(numbers)
        .retrieve()
        .bodyToMono(Void.class);
  }

}
