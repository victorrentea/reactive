package victor.training.reactive.demo.throttle;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.tcp.TcpClient;


@Slf4j
public class WebClientMaxConnections {

   private final String url;
   private final TcpClient tcpClient;

   public WebClientMaxConnections(String url, int maxConnections) {
      this.url = url;
      tcpClient = TcpClient.create(ConnectionProvider.builder("builder")
          .maxConnections(maxConnections)
          .pendingAcquireMaxCount(-1) // unbounded request waiting queue
          .build());

   }
   public Mono<String> makeRequest(int id) {
      WebClient webClient = WebClient.builder()
          .clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
          .build();
      return webClient.post()
          .uri(url)
          .accept(MediaType.ALL)
          .contentType(MediaType.TEXT_PLAIN)
          .syncBody("hello world")
          .retrieve().bodyToMono(String.class)
          .doOnNext(b -> log.info("Got request "+ id));
   }
}
