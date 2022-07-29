package victor.training.reactive.demo.spring;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitResult;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class InMemoryBroadcast {
   private AtomicInteger integer = new AtomicInteger(0);

   private Sinks.Many<ChatMessage> sink = Sinks.many().multicast()
           .onBackpressureBuffer();

   @GetMapping(value = "message/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
   public Flux<ServerSentEvent<ChatMessage>> messageStream(@RequestParam String topic) {
      return sink.asFlux().log()
          .filter(message -> message.getTopic().equals(Topic.valueOf(topic)))
          .map(message -> ServerSentEvent.builder(message).build());
   }

   @GetMapping("message/send")
   public void sendMessage() {
      Topic randTopic = Topic.values()[new Random().nextInt(2)];
      EmitResult emitResult = sink.tryEmitNext(new ChatMessage(randTopic, "Hello " + integer.incrementAndGet()));
      System.out.println("result: " + emitResult);
   }

   @Data
   @AllArgsConstructor
   @NoArgsConstructor
   static class CustomerDto {
      private String value;
   }
}

enum Topic {CHILD, WORK}
@Value
class ChatMessage {
   Topic topic;
   String message;
}