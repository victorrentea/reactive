package victor.training.reactive.demo.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.Unchecked;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@Component
public class WebSockets implements WebSocketHandler {

    private static final ObjectMapper jackson = new ObjectMapper();

    private static final Sinks.Many<ChatMessage> sink = Sinks.many().multicast().onBackpressureBuffer();

    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {
        log.info("Opened WS session: " + webSocketSession.getHandshakeInfo().getAttributes());
        return webSocketSession.send(
//                Flux.interval(Duration.ofMillis(100)).map(i-> "Message " + i).map(s -> new ChatMessage("me", s))

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


    @Bean
    public HandlerMapping webSocketHandlerMapping() {
        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put("/chat", this);

        SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
        handlerMapping.setOrder(1);
        handlerMapping.setUrlMap(map);
        return handlerMapping;
    }

    @Getter
    public static class ChatMessage {
        private String from;
        private String text;
        private String time =  LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        public ChatMessage() {}
        public ChatMessage(String from, String text) {
            this.from = from;
            this.text = text;
        }
    }
}