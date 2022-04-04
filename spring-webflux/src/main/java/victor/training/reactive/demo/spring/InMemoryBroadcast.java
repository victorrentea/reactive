package victor.training.reactive.demo.spring;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class InMemoryBroadcast {
   private AtomicInteger integer = new AtomicInteger(0);
   private Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer();


   @GetMapping(value = "message/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
   public Flux<ServerSentEvent<CustomerDto>> messageStream() {
      return sink.asFlux()
          .map(Objects::toString)
          .map(CustomerDto::new)
          .map(dto -> ServerSentEvent.builder(dto).build());
   }

   @GetMapping("message/send")
   public void sendMessage() {
      sink.tryEmitNext("Hello " + integer.incrementAndGet());
   }

   @Data
   @AllArgsConstructor
   @NoArgsConstructor
   static class CustomerDto {
      private String value;
   }
}
