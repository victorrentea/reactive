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
public class InMemoryBroadcastSSE {
  private AtomicInteger integer = new AtomicInteger(0);

  private Sinks.Many<ChatMessage> sink = Sinks.many().multicast().onBackpressureBuffer();

  // when a browser receives an HTTP response with Content-Type: */event-stream
  // it keeps the HTTP connection open waiting for server to push more response data
  @GetMapping(value = "message/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<ServerSentEvent<ChatMessage>> receive(
      @RequestParam(defaultValue = "WORK",required = false) Topic topic
  ) {
    return sink.asFlux()
        .log()
        .filter(message -> message.getTopic() == topic)
        .map(message -> ServerSentEvent.builder(message).build());
  }

  @GetMapping("message/send")
  public String send(
      @RequestParam(required = false) Topic topic
  ) {
    if (topic == null){
      topic = Topic.values()[new Random().nextInt(2)];
    }
    ChatMessage message = new ChatMessage(topic, "Hello " + integer.incrementAndGet());
    EmitResult emitResult = sink.tryEmitNext(message);
    System.out.println("result: " + emitResult);
    return "Sent: " + message;
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