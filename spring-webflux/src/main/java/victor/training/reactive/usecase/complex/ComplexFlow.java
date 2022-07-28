package victor.training.reactive.usecase.complex;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class ComplexFlow {

    // @TimedReactive // TODO
    public Mono<List<Product>> mainFlow(List<Long> productIds) {

        // 1: for ... + RestTemplate
        // 2: FP
        // 3: no .block()
        // ..... (see readme)

        throw new RuntimeException("Method not implemented");
    }
}
