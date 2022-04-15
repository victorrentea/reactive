package victor.training.reactive.demo.mdc;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.List;


@Slf4j
@Component
public class RequestFilter implements WebFilter {
   @Override
   public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
      ServerHttpRequest request = exchange.getRequest();
      String requestId = getRequestId(request.getHeaders());

      return chain
          .filter(exchange)
          .doOnEach(LogbackMDC.logOnEach(r -> log.info("Intercepted {} {}", request.getMethod(), request.getURI())))
          .contextWrite(Context.of(LogbackMDC.REACTOR_CONTEXT_KEY, requestId))
          // here
          ;
   }

   private String getRequestId(HttpHeaders headers) {
      List<String> requestIdHeaders = headers.get("X-Request-ID");
      if (requestIdHeaders != null && !requestIdHeaders.isEmpty()) {
         return requestIdHeaders.get(0);
      }
      return RandomStringUtils.randomAlphanumeric(8);
   }

}