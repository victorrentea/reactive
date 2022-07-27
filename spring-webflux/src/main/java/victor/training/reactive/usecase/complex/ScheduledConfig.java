package victor.training.reactive.usecase.complex;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
@EnableScheduling
public class ScheduledConfig {

    @Scheduled(fixedRate = 1000)
    public Mono<Integer> jobuLansatDinQuartzUnLibVechicareNUStieReact() {
        return Mono.fromSupplier(() -> {
            log.debug("Meeee too!");
            return 1;
        });
        // @Scheduled nu poate intoarce Publisher
    }

}
