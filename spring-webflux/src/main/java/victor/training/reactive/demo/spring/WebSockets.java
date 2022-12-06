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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;


@Slf4j
@Component
public class WebSockets implements WebSocketHandler {

  private static final ObjectMapper jackson = new ObjectMapper();

  private static final Sinks.Many<ChatMessage> sink = Sinks.many().multicast().onBackpressureBuffer(100);

  @Override
  public Mono<Void> handle(WebSocketSession webSocketSession) {
    // asa se ia userul pe bune: Mono<String> username = webSocketSession.getHandshakeInfo().getPrincipal().map(Principal::getName);

    log.info("Opened WS session: " + webSocketSession.getHandshakeInfo().getAttributes());
    Flux<WebSocketMessage> outboundFlux =
            sink.asFlux()
                    // daca mesajul contine vreun @xyz, atunci tre livrat la un user doar daca user din ses = 'xyz'
                    .filter(m -> filterOutPrivateMessagesNotForMe(m, (String) webSocketSession.getAttributes().get("user")))
                    .map(Unchecked.function(jackson::writeValueAsString))
                    .map(webSocketSession::textMessage)
                    .onErrorContinue(Exception.class, (e,o) -> {
                      log.warn("Tocmai am inghitit o " +
                               "exceptie aparute ORIUNDE deasupra in chain, lasand chainul sa emita mai departe" +
                               e + " la procesarea measajului  " + o);
                    });

    Flux<ChatMessage> inboundFlux = webSocketSession.receive()
            .map(WebSocketMessage::getPayloadAsText)
            .map(Unchecked.function(json -> jackson.readValue(json, ChatMessage.class)))
            .filter(m -> {
              if (m.from.equals("sys")) {
                String nick = m.text;
                log.info("A venit user " + nick);
                webSocketSession.getAttributes().put("user", nick);
                return false;
              } else {
                return true;
              }
            })

            .doOnNext(m -> sink.tryEmitNext(m))
            .log();

    return webSocketSession.send(outboundFlux).and(inboundFlux);
  }

  private boolean filterOutPrivateMessagesNotForMe(ChatMessage m, String user) {
    if (Math.random() > .5)throw new IllegalArgumentException();
    Optional<String> tokenAt = Stream.of(m.text.split("\\s+"))
            .filter(token -> token.contains("@"))
            .findFirst();
    if (tokenAt.isEmpty()) return true;
    return tokenAt.get().equals("@" + user);
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
    private String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));

    public ChatMessage() {
    }

    public ChatMessage(String from, String text) {
      this.from = from;
      this.text = text;
    }
  }
}