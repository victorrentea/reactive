package victor.training.reactor.workshop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import victor.training.reactor.workshop.P9_ComplexFlow.*;
import victor.training.util.RunAsNonBlocking;
import victor.training.util.SubscribedProbe;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.mockito.Mockito.when;

@TestMethodOrder(MethodName.class)
@ExtendWith(MockitoExtension.class)
public class P9_ComplexFlowTest {
  public static final int ID = 13;
  @Mock
  Dependency dependency;
  @InjectMocks
  P9_ComplexFlow workshop;
//  P9_ComplexFlowSolved workshop;
  @RegisterExtension
  SubscribedProbe subscribed = new SubscribedProbe();

  @BeforeEach
  final void before() {
    when(dependency.a(ID)).thenReturn(subscribed.once(Mono.just(new A("a"))));
    when(dependency.d(ID)).thenReturn(subscribed.once(Mono.just(new D("d"))));
    when(dependency.b(new A("a"))).thenReturn(subscribed.once(Mono.just(new B("b"))));
    when(dependency.c(new A("a"))).thenReturn(subscribed.once(Mono.just(new C("c"))));
    when(dependency.saveA(new A("aBcd"))).thenReturn(subscribed.once(Mono.just(new A("asf"))));
    when(dependency.auditA(new A("aBcd"), new A("a"))).thenReturn(subscribed.once(Mono.delay(ofMillis(200)).then()));
  }

  @Test
  void functionalCorrect() throws ExecutionException, InterruptedException {
    workshop.p06_complexFlow(ID).block();
  }

  @Test
  @Timeout(value = 150, unit = TimeUnit.MILLISECONDS)
  void doesNotWaitForAudit() throws ExecutionException, InterruptedException {
    workshop.p06_complexFlow(ID).block();
  }

  @Test
  void doesNotFailForAudit() throws ExecutionException, InterruptedException {
    subscribed.clearAllProbes();
    when(dependency.auditA(new A("aBcd"), new A("a"))).thenReturn(Mono.error(new RuntimeException()));
    workshop.p06_complexFlow(ID).block();
  }

  @Test
  public void doesNotBlock() {
    RunAsNonBlocking.nonBlocking(() -> {
      try {
        return workshop.p06_complexFlow(ID);
      } catch (ExecutionException | InterruptedException e) {
        throw new RuntimeException(e);
      }
    }).block();
  }

  @Test
  @Timeout(value = 500, unit = TimeUnit.MILLISECONDS)
  public void callsB_parallel_C() throws ExecutionException, InterruptedException {
    subscribed.clearAllProbes();
    when(dependency.b(new A("a"))).thenReturn(Mono.just(new B("b")).delayElement(ofMillis(300)));
    when(dependency.c(new A("a"))).thenReturn(Mono.just(new C("c")).delayElement(ofMillis(300)));

    workshop.p06_complexFlow(ID).block();
  }

}
