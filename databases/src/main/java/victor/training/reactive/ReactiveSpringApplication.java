package victor.training.reactive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuples;

import java.util.List;

@SpringBootApplication
public class ReactiveSpringApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReactiveSpringApplication.class, args);
	}

	@Bean
	public RestTemplate rest() {
	    return new RestTemplate();
	}


	// vechiul servlet Filter
	@Bean
	public WebFilter alwaysParallelWebfluxFilter() {
		return new WebFilter() {
			@Override
			public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
				System.out.println("Filter HTTP: " + exchange.getRequest().getURI());

				return chain.filter(exchange) // to springu ruleaza aici !!!
								// eu sunt la final ca un boss si pun in context ,
								// ca ala se propaga doar in sus pe semnalul de subscribe
								.contextWrite(context -> context.put("tenantId", "73"));
			}
		};
	}
}
//class X implements Filter


