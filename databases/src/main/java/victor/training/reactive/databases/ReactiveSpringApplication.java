package victor.training.reactive.databases;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class ReactiveSpringApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReactiveSpringApplication.class, args);
	}

	@Bean
	public RestTemplate rest() {
	    return new RestTemplate();
	}
}


