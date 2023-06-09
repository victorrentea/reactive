package victor.training.reactor.workshop;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.platform.commons.function.Try;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import victor.training.reactor.lite.Utils;
import victor.training.reactor.workshop.C4_SideEffects.A;
import victor.training.reactor.workshop.C4_SideEffects.AStatus;
import victor.training.reactor.workshop.C4_SideEffects.Dependency;
import victor.training.util.CaptureSystemOutputExtension;
import victor.training.util.SubscribedProbe;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import static java.time.Duration.ofMillis;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static victor.training.util.RunAsNonBlocking.nonBlocking;

@Slf4j
@TestMethodOrder(MethodName.class)
@ExtendWith(MockitoExtension.class)
public class C4_SideEffectsTest {
  @Mock
  Dependency dependency;
  @InjectMocks
  protected C4_SideEffects workshop;

  @RegisterExtension
  SubscribedProbe subscribed = new SubscribedProbe();
  @RegisterExtension
  CaptureSystemOutputExtension systemOutput = new CaptureSystemOutputExtension();

  private static final A a0 = new A();
  private static final A a = new A();

  @Test
  void p01_sendMessageAndReturn() {
    Mono<A> ma = subscribed.once(Mono.just(a0));
    when(dependency.sendMessage(a0)).thenReturn(subscribed.once(Mono.empty()));

    assertThat(nonBlocking(() -> workshop.p01_sendMessageAndReturn(ma))).isEqualTo(a0);
  }

  @Test
  void p02_saveAndSend() {
    when(dependency.save(a0)).thenReturn(subscribed.once(Mono.just(a)));
    when(dependency.sendMessage(a)).thenReturn(subscribed.once(Mono.empty()));

    nonBlocking(() -> workshop.p02_saveAndSend(a0));
  }

  @Test
  void p03_saveSendIfUpdated_updated() {
    a.updated = true;
    when(dependency.save(a0)).thenReturn(subscribed.once(Mono.just(a)));
    when(dependency.sendMessage(a)).thenReturn(subscribed.once(Mono.empty()));

    nonBlocking(() -> workshop.p03_saveSendIfUpdated(a0));
  }

  @Test
  void p03_saveSendIfUpdated_notUpdated() {
    a.updated = false;
    when(dependency.save(a0)).thenReturn(subscribed.once(Mono.just(a)));
    // Note: org.mockito.configuration.DefaultErrorSignalException makes all non-stubbed methods to emits an error signal by default

    nonBlocking(() -> workshop.p03_saveSendIfUpdated(a0));
  }


  @Test
  void p04_saveSendIfConflictRemote_conflict() {
    when(dependency.save(a0)).thenReturn(subscribed.once(Mono.just(a)));
    when(dependency.retrieveStatus(a)).thenReturn(subscribed.once(Mono.just(AStatus.CONFLICT)));
    when(dependency.sendMessage(a)).thenReturn(subscribed.once(Mono.empty()));

    nonBlocking(() -> workshop.p04_saveSendIfConflictRemote(a0));
  }

  @Test
  void p04_saveSendIfConflictRemote_notCONFLICT() {
    when(dependency.save(a0)).thenReturn(subscribed.once(Mono.just(a)));
    when(dependency.retrieveStatus(a)).thenReturn(subscribed.once(Mono.just(AStatus.NEW)));
    // [hard] line not needed due to  org.mockito.configuration.MockitoConfiguration
    // which configures all mocked methods returning publisher to return a Mono/Flux.error by default
//    when(dependency.sendMessage(a)).thenReturn(Mono.error(new IllegalArgumentException("Should never be subscribed")));

    nonBlocking(() -> workshop.p04_saveSendIfConflictRemote(a0));
  }

  @Test
  void p05_saveSendAuditReturn() {
    when(dependency.save(a0)).thenReturn(subscribed.once(Mono.just(a)));
    when(dependency.sendMessage(a)).thenReturn(subscribed.once(Mono.empty()));
    when(dependency.audit(a)).thenReturn(subscribed.once(Mono.empty()));

    nonBlocking(() -> workshop.p05_saveSendAuditReturn(a0));
  }

  @Test
  void p06_ignoreError_allOK() {
    when(dependency.save(a0)).thenReturn(subscribed.once(Mono.just(a)));
    when(dependency.sendMessage(a)).thenReturn(subscribed.once(Mono.empty()));
    when(dependency.audit(a)).thenReturn(subscribed.once(Mono.empty()));

    assertThat(nonBlocking(() -> workshop.p06_ignoreError(a0))).isEqualTo(a);
  }

  @Test
  void p06_ignoreError_whenSendMessageFails() {
    when(dependency.save(a0)).thenReturn(subscribed.once(Mono.just(a)));
    when(dependency.sendMessage(a)).thenReturn(subscribed.once(Mono.error(new IllegalArgumentException())));
    when(dependency.audit(a)).thenReturn(subscribed.once(Mono.empty()));

    assertThat(nonBlocking(() -> workshop.p06_ignoreError(a0))).isEqualTo(a);
  }

  @Test
  @Timeout(1)
  void p07_parallel() {
    when(dependency.save(a0)).thenReturn(subscribed.once(Mono.just(a)));
    when(dependency.sendMessage(a)).thenReturn(subscribed.once(Mono.delay(ofMillis(600)).then()));
    when(dependency.audit(a)).thenReturn(subscribed.once(Mono.delay(ofMillis(600)).then()));

    assertThat(nonBlocking(() -> workshop.p07_parallel(a0))).isEqualTo(a);
  }

  @Test
  void p08_fireAndForget() {
    when(dependency.save(a0)).thenReturn(subscribed.once(Mono.just(a)));
    when(dependency.sendMessage(a)).thenReturn(subscribed.once(Mono.empty()));

    assertThat(nonBlocking(() -> workshop.p08_fireAndForget(a0))).isEqualTo(a);
  }

  @Test
  void p08_fireAndForget_errorsSilencedButLogged() {
    when(dependency.save(a0)).thenReturn(subscribed.once(Mono.just(a)));
    when(dependency.sendMessage(a)).thenReturn(subscribed.once(Mono.error(new IllegalStateException("TEST ERROR"))));

    assertThat(nonBlocking(() -> workshop.p08_fireAndForget(a0))).isEqualTo(a);
    Utils.sleep(300);
    assertThat(systemOutput.toString()).contains("TEST ERROR");
  }

  @Test
  @Timeout(1)
  void p08_fireAndForget_notWaited() {
    when(dependency.save(a0)).thenReturn(subscribed.once(Mono.just(a)));
    when(dependency.sendMessage(a)).thenReturn(subscribed.once(Mono.delay(ofMillis(2000)).then()));

    assertThat(nonBlocking(() -> workshop.p08_fireAndForget(a0))).isEqualTo(a);
  }

  @Test
  @Timeout(1)
  void p08_fireAndForget_propagatesReactorContext() throws Exception {
    ArrayBlockingQueue<Try<String>> tryContextValueHolder = new ArrayBlockingQueue<>(1);
    when(dependency.save(a0)).thenReturn(subscribed.once(Mono.just(a)));
    when(dependency.sendMessage(a)).thenReturn(subscribed.once(Mono.deferContextual(context -> {
      tryContextValueHolder.offer(Try.call(() -> context.get("context-key")));
      return Mono.empty();
    })));

    workshop.p08_fireAndForget(a0)
        .contextWrite(context -> context.put("context-key", "context-value"))
        .block();

    Try<String> tryContextValue = tryContextValueHolder.poll(200, TimeUnit.MILLISECONDS);
    assertThat(tryContextValue.get()).isEqualTo("context-value");
  }
}
