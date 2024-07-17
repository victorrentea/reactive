package victor.training.reactive;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@RestController
@RequiredArgsConstructor
public class Client {
  @GetMapping("/vali")
  public Flux<Integer> trimitLaUnServerGrameziDeDate() {
//    List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    //10000 elemente
    List<Integer> numbers = IntStream.range(0, 100).boxed().collect(Collectors.toList());
    return Flux.fromIterable(numbers)
        .buffer(5)
        .flatMap(this::sendChunk, 1) // max 3 pag odata trimit
        ;
  }

  private Flux<Integer> sendChunk(List<Integer> numbers) {
    // cerinta daca crapa o pagina de 10,
    // incearca sa trimiti cate 1 element odata
    return send(numbers)
        .thenMany(Flux.<Integer>empty())
//        .sample(Duration.ofMillis(50))
//        .onBackpressureBuffer()
        .onErrorResume(err ->
            Flux.fromIterable(numbers)
                .flatMap(element -> send(List.of(element))
                    .doOnError(e-> log.error("EROARE LA " + element))
                    .thenMany(Flux.<Integer>empty())
                    .onErrorResume(e-> Flux.just(element))
                )
        );
  }

  private @NotNull Mono<Void> send(List<Integer> numbers) {
    return WebClient.create().post().uri("http://localhost:8080/process")
        .bodyValue(numbers)
        .retrieve()
        .bodyToMono(Void.class);
  }

}
