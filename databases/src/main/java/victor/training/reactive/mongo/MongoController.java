package victor.training.reactive.mongo;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("mongo")
public class MongoController {
   private final EventReactiveRepo rxRepo;
   private final EventBlockingRepo blockingRepo;

   @GetMapping("mono")
   public Mono<List<Event>> mono() {
      return rxRepo.findAll().collectList();
   }

   @GetMapping("flux")
   public Flux<Event> flux() {
      return rxRepo.findAll();
   }

   @GetMapping("send")
   public Mono<String> sendMessage() {
      Event event = new Event("Message at " + LocalDateTime.now());
      return rxRepo.save(event).map(Event::getId);
   }

   @GetMapping(value = "flux-live", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
   public Flux<ServerSentEvent<String>> messageStream() {
      return rxRepo.findAllByIdNotNull()
              .map(Event::getValue)
              .map(b -> ServerSentEvent.builder(b).build());
   }
}

