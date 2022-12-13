package victor.training.reactor.workshop;

import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import victor.training.reactor.lite.Utils;
import victor.training.reactor.workshop.P4_SideEffects.A;
import victor.training.reactor.workshop.P4_SideEffects.AStatus;
import victor.training.reactor.workshop.P4_SideEffects.Dependency;
import victor.training.util.CaptureSystemOutput;
import victor.training.util.CaptureSystemOutput.OutputCapture;
import victor.training.util.SubscribedProbe;

import static java.time.Duration.ofMillis;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static victor.training.util.RunAsNonBlocking.nonBlocking;

@TestMethodOrder(MethodName.class)
@ExtendWith(MockitoExtension.class)
public class P4_SideEffectsTest {
  @Mock
  Dependency dependency;
  @InjectMocks
//  P4_SideEffects workshop;
  P4_SideEffectsSolved workshop;
  @RegisterExtension
  SubscribedProbe subscribed = new SubscribedProbe();

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
    when(dependency.save(a0)).thenReturn(subscribed.once(Mono.just(a)));
    when(dependency.sendMessage(a)).thenReturn(subscribed.once(Mono.empty()));
    when(dependency.audit(a)).thenReturn(subscribed.once(Mono.empty()));

    nonBlocking(() -> workshop.p04_saveSendAuditReturn(a0)).block();
  }
  @Test
  void p04_saveSendAuditReturnNotUpdated() {
    a.updated=false;
    when(dependency.save(a0)).thenReturn(subscribed.once(Mono.just(a)));
    // Note: org.mockito.configuration.DefaultErrorSignalException makes all non-stubbed methods to emits an error signal by default

    nonBlocking(() -> workshop.p04_saveSendAuditReturn(a0)).block();
  }

  @Test
  void p05_saveSendAuditKOReturn() {
    when(dependency.save(a0)).thenReturn(subscribed.once(Mono.just(a)));
    when(dependency.sendMessage(a)).thenReturn(subscribed.once(Mono.empty()));
    when(dependency.audit(a)).thenReturn(subscribed.once(Mono.empty()));

    assertThat(nonBlocking(() -> workshop.p05_saveSendAuditKOReturn(a0)).block()).isEqualTo(a);
  }

  @Test
  void p05_saveSendAuditKOReturn_KO() {
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

    assertThat(nonBlocking(() -> workshop.p07_save_sendFireAndForget(a0)).block()).isEqualTo(a);
  }

  @Test
  @CaptureSystemOutput
  void p07_save_sendFireAndForget_errorsSilencedButLogged(OutputCapture outputCapture) {
    when(dependency.save(a0)).thenReturn(subscribed.once(Mono.just(a)));
    when(dependency.sendMessage(a)).thenReturn(subscribed.once(Mono.error(new IllegalStateException("TEST ERROR"))));

    assertThat(nonBlocking(() -> workshop.p07_save_sendFireAndForget(a0)).block()).isEqualTo(a);
    Utils.sleep(300);
    assertThat(outputCapture.toString()).contains("TEST ERROR");
  }

  @Test
  @Timeout(1)
  void p07_save_sendFireAndForget_notWaited() {
    when(dependency.save(a0)).thenReturn(subscribed.once(Mono.just(a)));
    when(dependency.sendMessage(a)).thenReturn(subscribed.once(Mono.delay(ofMillis(2000)).then()));

    assertThat(nonBlocking(() -> workshop.p07_save_sendFireAndForget(a0)).block()).isEqualTo(a);
  }
}
