package victor.training.reactive.usecase.complex;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import victor.training.reactive.Utils;

import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@Slf4j
//@EnableScheduling
public class ScheduledConfig {
    AtomicInteger a = new AtomicInteger(1);
    @Scheduled(fixedRate = 1000)
    //    @Scheduled(cron = "* * * * /1 * ")
    public void job() throws InterruptedException {
        Mono.just(1).publishOn(Schedulers.parallel())
                .map(date -> {
                    int id = a.incrementAndGet();
                    log.debug("Start JOB "+id);
                    Utils.sleep(1500);
                    log.debug("END JOB " + id);
//                    log.debug("De fapt se cheama functia asta de framework la secunda, dar nimeni nu subscrie la publisherul tau");
//                    log.debug("Meeee too!");
                    return 1;
                }).block();
        // @Scheduled nu poate intoarce Publisher
    }

}
