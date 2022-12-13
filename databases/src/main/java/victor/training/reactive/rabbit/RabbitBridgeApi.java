package victor.training.reactive.rabbit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.rabbitmq.Receiver;

import javax.annotation.PostConstruct;
import java.time.Duration;

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
}
