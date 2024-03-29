package victor.training.reactive.demo.mdc_lifter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;


@Slf4j
@Component
public class RequestFilterInsertingMDCInContext implements WebFilter {
   @Override
   public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
      String requestId = getRequestId(exchange.getRequest().getHeaders());
      return chain.filter(exchange)
          .contextWrite(context -> context.put("MDC_KEY", requestId))
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