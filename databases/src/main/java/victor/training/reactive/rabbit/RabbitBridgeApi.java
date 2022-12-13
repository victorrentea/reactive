package victor.training.reactive.rabbit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.Sender;
import victor.training.reactive.mongo.EventReactiveRepo;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicReference;

@RestController
public class RabbitBridgeApi {

  @Autowired
  private Receiver receiver;

  private ConnectableFlux<String> hotFluxDeMesajeRabbit;
  @PostConstruct
  public void initHotFlux() {
    hotFluxDeMesajeRabbit = receiver.consumeAutoAck("demo-queue")
            .map(m->new String(m.getBody()))
            .publish(); /*Flux.interval(Duration.ofSeconds(1)).publish();*/
    hotFluxDeMesajeRabbit.connect();
  }

  @GetMapping(value = "trenuri",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<String> trenuri(@RequestParam(defaultValue = "a") String text) {
    return hotFluxDeMesajeRabbit
            .filter(m->m.contains(text))
//            .map(m->m.toUpperCase())
            ;
//            .flatMap(m-> enrichWithDataMaybeFromCache)
  }

  @GetMapping("send")
  public Mono<String> sendPeRabbit() { // 1! UN endpoint care NU intoarce Mono/Flux <=> nu atinge retea
    AtomicReference<String> nucumva= new AtomicReference<>();
    return Mono.deferContextual(context -> Mono.just((String) context.get("tenantId")))
            .map(tenantId -> "Thales Rupe pe " + tenantId + "! la ora " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")))
            .doOnNext(body -> nucumva.set(body))
            .map(body -> new OutboundMessage("", "demo-queue", body.getBytes()))

            .flatMap(message -> sender.send(Mono.justOrEmpty(message)))
            .then(Mono.fromCallable(() -> "Sent message: " + nucumva.get()));
  }
  // de ce e Publisher() param? => ca sa poti conecta un flux din alta parte in send()

  @Autowired
  private EventReactiveRepo reactiveRepo;

  @Autowired
  private Sender sender;
}
