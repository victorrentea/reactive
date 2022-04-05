package victor.training.reactive.databases.mongo;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("mongo")
public class MongoController {
   private final EventReactiveRepo rxRepo;
   private final EventBlockingRepo blockingRepo;

   @GetMapping(value = "flux-live", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
   public Flux<ServerSentEvent<String>> messageStream() {
      return rxRepo.findAllByIdNotNull()
          .map(Event::getValue)
          .map(b -> ServerSentEvent.builder(b).build());
   }

   @GetMapping(value = "flux")
   public Flux<String> flux() {
      return rxRepo.findAll().map(Objects::toString);
   }

   @GetMapping(value = "mono")
   public Mono<String> mono() {
      return rxRepo.findAll().collectList().map(Objects::toString);
   }

   @GetMapping(value = "list")
   public Mono<String> list() {
      List<Event> result = new ArrayList<>();
      blockingRepo.findAll().forEach(result::add);
      return Mono.just(result.toString());
   }

   @GetMapping("create")
   public Mono<String> sendMessage() {
      Event event = new Event("Aloha " + LocalDateTime.now());
      return rxRepo.save(event).map(Event::getId);
   }
}

