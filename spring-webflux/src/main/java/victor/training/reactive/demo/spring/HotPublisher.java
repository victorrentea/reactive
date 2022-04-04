package victor.training.reactive.demo.spring;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Objects;

@RestController
public class HotPublisher {
   private ConnectableFlux<Long> hotFlux;

   @PostConstruct
   public void initHotFlux() {
      hotFlux = Flux.interval(Duration.ofSeconds(1)).publish();
      hotFlux.connect();
   }

   @GetMapping(value = "tick", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
   public Flux<String> tick() {
      return hotFlux.map(Objects::toString);
   }
}
