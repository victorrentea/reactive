package victor.training.reactor.workshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.net.URI;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.temporaryRedirect;

@SpringBootApplication
public class SpringApp  {
  public static void main(String[] args) {
      SpringApplication.run(SpringApp.class, args);
  }
  @Bean
  public RouterFunction<ServerResponse> routes() {
    return route(GET("/"), req -> temporaryRedirect(URI.create("/swagger-ui.html")).build());
  }
}
