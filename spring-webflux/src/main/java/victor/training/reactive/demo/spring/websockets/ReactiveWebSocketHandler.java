package victor.training.reactive.demo.spring.websockets;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.Unchecked;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Slf4j
@Component
public class ReactiveWebSocketHandler implements WebSocketHandler {

    private static final ObjectMapper jackson = new ObjectMapper();

    private static final Sinks.Many<ChatMessage> sink = Sinks.many().multicast().onBackpressureBuffer();

    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {
        log.info("Opened WS session: " + webSocketSession.getHandshakeInfo().getAttributes());
        return webSocketSession.send(
//                Flux.interval(Duration.ofMillis(100))
//                        .map(i-> "Message " + i)
//                        .map(s -> new ChatMessage("me", s))
                        sink.asFlux()
                        .map(Unchecked.function(jackson::writeValueAsString))
                        .map(webSocketSession::textMessage)
                )

          .and(webSocketSession.receive()
            .map(WebSocketMessage::getPayloadAsText)
            .map(Unchecked.function(json-> jackson.readValue(json, ChatMessage.class)))
            .doOnNext(message-> sink.tryEmitNext(message))
            .log());
    }


    public static class ChatMessage {

        private String from;
        private String text;
        private String time =  LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        public ChatMessage() {}
        public ChatMessage(String from, String text) {

            this.from = from;
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public String getTime() {
            return time;
        }

        public String getFrom() {
            return from;
        }
    }
}