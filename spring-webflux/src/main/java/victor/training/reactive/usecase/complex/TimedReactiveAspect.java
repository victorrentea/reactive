package victor.training.reactive.usecase.complex;


import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.Duration;

@Component
@Aspect
@Slf4j
public class TimedReactiveAspect {
    @Retention(RetentionPolicy.RUNTIME)
    public @interface TimedReactive {
    }

    @Autowired
    private MeterRegistry simpleMeterRegistry;

    @Around("@annotation(victor.training.reactive.usecase.complex.TimedReactiveAspect.TimedReactive)")
    public Object method(ProceedingJoinPoint pjp) throws Throwable {
        String metricName = pjp.getSignature().getName()+"-elapsed";

        Timer timer = simpleMeterRegistry.timer(metricName);
        timer.record(Duration.ofMillis(100));

        Object ret = pjp.proceed();
        if (ret instanceof Mono<?>) {
            Mono<?> mono = (Mono<?>) ret;
            // TODO measure and timer.record(...) the time elapsed between subscribe and next signals
            return mono;
            // SOLUTION: return mono.timed().doOnNext(timed -> timer.record(timed.elapsedSinceSubscription())).map(Timed::get);
        }
        return ret;
    }
}
