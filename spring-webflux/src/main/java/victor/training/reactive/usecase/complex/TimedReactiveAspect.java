package victor.training.reactive.usecase.complex;


import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Timed;

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
    private MeterRegistry meterRegistry;

    @Around("@annotation(victor.training.reactive.usecase.complex.TimedReactiveAspect.TimedReactive)")
    public Object method(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed(); // real method call

        Timer timer = meterRegistry.timer(pjp.getSignature().getName() + "-elapsed");

        if (result instanceof Mono<?>) {
            Mono<?> mono = (Mono<?>) result;
            timer.record(Duration.ofMillis(100)); // example: find it in /actuator/prometheus
            // TODO #1 timer.record(...) the time elapsed between subscribe and next signals
            return mono;
            // SOLUTION: return mono.timed().doOnNext(timed -> timer.record(timed.elapsedSinceSubscription())).map(Timed::get);
        } else if (result instanceof Flux<?>) {
            Flux<?> flux = (Flux<?>) result;
            // TODO #2 timer.record(...) the time elapsed between subscribe and completion signals
            //      (Hint: .timed() won't help anymore)
            throw new IllegalArgumentException("not implemented ");
        }
        return result;
    }
}
