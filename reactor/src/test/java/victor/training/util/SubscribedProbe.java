package victor.training.util;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.PublisherProbe;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
// TODO copy this to my project
public class SubscribedProbe implements InvocationInterceptor {
  @Value
  private static class ProbeSpec {
    PublisherProbe<?> probe;
    String testClassLine;
    int times;
  }

  private final List<ProbeSpec> probes = new ArrayList<>();

  public <T> Mono<T> once(Mono<T> mono) {
    PublisherProbe<T> probe = createAndRecordProbe(mono, 1);
    return probe.mono();
  }
  public <T> Flux<T> once(Flux<T> mono) {
    PublisherProbe<T> probe = createAndRecordProbe(mono, 1);
    return probe.flux();
  }

  public <T> Mono<T> times(int subscribed, Mono<T> mono) {
    PublisherProbe<T> probe = createAndRecordProbe(mono, subscribed);
    return probe.mono();
  }
  public <T> Flux<T> times(int subscribed, Flux<T> mono) {
    PublisherProbe<T> probe = createAndRecordProbe(mono, subscribed);
    return probe.flux();
  }

  private <T> PublisherProbe<T> createAndRecordProbe(Publisher<T> mono, int times) {
    StackTraceElement callerStackTraceElement = new RuntimeException().getStackTrace()[2];
    PublisherProbe<T> probe = PublisherProbe.of(mono);
    String testClassLine = callerStackTraceElement.toString();
    probes.add(new ProbeSpec(probe, testClassLine, times));
    return probe;
  }

  @Override
  public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
    invocation.proceed();

    for (ProbeSpec probeSpec : probes) {
      assertThat(probeSpec.probe.subscribeCount())
              .describedAs("Publisher was subscribed too many (or few) times\n  Publisher probed in test: "+probeSpec.testClassLine)
              .isEqualTo(probeSpec.times);
    }
  }

  public void clearAllProbes() {
    probes.clear();
  }
}
