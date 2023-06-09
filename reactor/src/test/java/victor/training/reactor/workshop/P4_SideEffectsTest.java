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
import reactor.test.publisher.PublisherProbe;
import reactor.test.publisher.TestPublisher;
import victor.training.reactor.lite.Utils;
import victor.training.reactor.workshop.P4_SideEffects.A;
import victor.training.reactor.workshop.P4_SideEffects.AStatus;
import victor.training.reactor.workshop.P4_SideEffects.Dependency;
import victor.training.util.CaptureSystemOutputExtension;
import victor.training.util.NonBlockingTest;
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
public class P4_SideEffectsTest {
  @Mock
  Dependency dependency;
  @InjectMocks
  protected P4_SideEffects workshop;

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

    assertThat(nonBlocking(() -> workshop.p01_sendMessageAndReturn(ma)).block()).isEqualTo(a0);
  }

  @Test
  void p02_saveAndSend() {
    when(dependency.save(a0)).thenReturn(subscribed.once(Mono.just(a)));
    when(dependency.sendMessage(a)).thenReturn(subscribed.once(Mono.empty()));

    nonBlocking(() -> workshop.p02_saveAndSend(a0)).block();
  }

  @Test
  void p03_saveSendIfConflict() {
    when(dependency.save(a0)).thenReturn(subscribed.once(Mono.just(a)));
    when(dependency.retrieveStatus(a)).thenReturn(subscribed.once(Mono.just(AStatus.CONFLICT)));
    when(dependency.sendMessage(a)).thenReturn(subscribed.once(Mono.empty()));

    nonBlocking(() -> workshop.p03_saveSendIfConflict(a0)).block();
  }
  @Test
  void p03_saveSendIfConflict_notCONFLICT() {
    when(dependency.save(a0)).thenReturn(subscribed.once(Mono.just(a)));
    when(dependency.retrieveStatus(a)).thenReturn(subscribed.once(Mono.just(AStatus.NEW)));
    // [hard] line not needed due to  org.mockito.configuration.MockitoConfiguration
    // which configures all mocked methods returning publisher to return a Mono/Flux.error by default
//    when(dependency.sendMessage(a)).thenReturn(Mono.error(new IllegalArgumentException("Should never be subscribed")));

    nonBlocking(() -> workshop.p03_saveSendIfConflict(a0)).block();
  }
  @Test
  void p04_saveSendAuditReturn() {
    a.updated=true;
    PublisherProbe<Void> sendProbe = PublisherProbe.empty();
    when(dependency.save(a0)).thenReturn(subscribed.once(Mono.just(a)));
    when(dependency.sendMessage(a)).thenReturn(sendProbe.mono());
    when(dependency.audit(a)).thenReturn(subscribed.once(Mono.empty()));

    nonBlocking(() -> workshop.p04_saveSendAuditReturn(a0)).block();
    sendProbe.assertWasSubscribed();
  }
  @Test
  void p04_saveSendAuditReturnNotUpdated() {
    a.updated=false;
    PublisherProbe<A> probe = PublisherProbe.of(Mono.just(a));
    when(dependency.save(a0)).thenReturn(probe.mono());
//    when(dependency.save(a0)).thenReturn(subscribed.once(Mono.just(a)));
    // Note: org.mockito.configuration.DefaultErrorSignalException makes all non-stubbed methods to emits an error signal by default

    nonBlocking(() -> workshop.p04_saveSendAuditReturn(a0)).block();
    probe.assertWasSubscribed();
  }

  @Test
  void p05_saveSendAuditOKReturn() {
    a.updated = true;
    when(dependency.save(a0)).thenReturn(subscribed.once(Mono.just(a)));
    when(dependency.sendMessage(a)).thenReturn(subscribed.once(Mono.empty()));
    when(dependency.audit(a)).thenReturn(subscribed.once(Mono.empty()));

    assertThat(nonBlocking(() -> workshop.p05_saveSendAuditKOReturn(a0)).block()).isEqualTo(a);
  }

  @Test
  void p05_saveSendAuditKOReturn_KO() {
    a.updated = true;
    when(dependency.save(a0)).thenReturn(subscribed.once(Mono.just(a)));
    when(dependency.sendMessage(a)).thenReturn(subscribed.once(Mono.error(new IllegalArgumentException())));
    when(dependency.audit(a)).thenReturn(subscribed.once(Mono.empty()));

    assertThat(nonBlocking(() -> workshop.p05_saveSendAuditKOReturn(a0)).block()).isEqualTo(a);
  }

  @Test
  @Timeout(1)
  void p06_saveSend_par_AuditReturn() {
    when(dependency.save(a0)).thenReturn(subscribed.once(Mono.just(a)));
    when(dependency.sendMessage(a)).thenReturn(subscribed.once(Mono.delay(ofMillis(600)).then()));
    when(dependency.audit(a)).thenReturn(subscribed.once(Mono.delay(ofMillis(600)).then()));

    assertThat(nonBlocking(() -> workshop.p06_saveSend_par_AuditReturn(a0)).block()).isEqualTo(a);
  }

  @Test
  void p07_save_sendFireAndForget() {
    when(dependency.save(a0)).thenReturn(subscribed.once(Mono.just(a)));
    when(dependency.sendMessage(a)).thenReturn(subscribed.once(Mono.empty()));

    assertThat(nonBlocking(() -> workshop.p07_save_sendFireAndForget(a0)
        .contextWrite(context->context.put("context-key", "context-value"))
    ).block()).isEqualTo(a);
  }

  @Test
  void p07_save_sendFireAndForget_errorsSilencedButLogged() {
    when(dependency.save(a0)).thenReturn(subscribed.once(Mono.just(a)));
    when(dependency.sendMessage(a)).thenReturn(subscribed.once(Mono.error(new IllegalStateException("TEST ERROR"))));

    assertThat(nonBlocking(() -> workshop.p07_save_sendFireAndForget(a0)
        .contextWrite(context->context.put("context-key", "context-value"))
    ).block()).isEqualTo(a);
    Utils.sleep(300);
    assertThat(systemOutput.toString()).contains("TEST ERROR");
  }

  @Test
  @Timeout(1)
  void p07_save_sendFireAndForget_notWaited() {
    when(dependency.save(a0)).thenReturn(subscribed.once(Mono.just(a)));
    when(dependency.sendMessage(a)).thenReturn(subscribed.once(Mono.delay(ofMillis(2000)).then()));

    assertThat(nonBlocking(() -> workshop.p07_save_sendFireAndForget(a0)
        .contextWrite(context->context.put("context-key", "context-value"))
    ).block()).isEqualTo(a);
  }

  @Test
  @Timeout(1)
  void p07_save_sendFireAndForget_propagatesReactorContext() throws Exception {
    ArrayBlockingQueue<Try<String>> tryContextValueHolder = new ArrayBlockingQueue<>(1);
    when(dependency.save(a0)).thenReturn(subscribed.once(Mono.just(a)));
    when(dependency.sendMessage(a)).thenReturn(subscribed.once(Mono.deferContextual(context -> {
      tryContextValueHolder.offer(Try.call(() ->context.get("context-key")));
      return Mono.empty();
    })));

   workshop.p07_save_sendFireAndForget(a0)
      .contextWrite(context->context.put("context-key", "context-value"))
      .block();

    Try<String> tryContextValue = tryContextValueHolder.poll(200, TimeUnit.MILLISECONDS);
    assertThat(tryContextValue.get()).isEqualTo("context-value");
  }
}
