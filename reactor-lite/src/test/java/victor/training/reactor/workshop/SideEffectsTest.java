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
import reactor.core.publisher.Mono;
import victor.training.reactor.lite.Utils;
import victor.training.reactor.workshop.SideEffects.A;
import victor.training.reactor.workshop.SideEffects.Dependency;
import victor.training.util.CaptureSystemOutput;
import victor.training.util.CaptureSystemOutput.OutputCapture;
import victor.training.util.SubscribedProbe;

import static java.time.Duration.ofMillis;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static victor.training.util.RunAsNonBlocking.runsNonBlocking;

@TestMethodOrder(MethodName.class)
@ExtendWith(MockitoExtension.class)
public class SideEffectsTest {
  @Mock
  Dependency dependency;
  @InjectMocks
  SideEffects workshop;
  @RegisterExtension
  SubscribedProbe subscribed = new SubscribedProbe();

  private static final A a0 = new A();
  private static final A a = new A();

  @Test
  void p01_sendMessageAndReturn() {
    Mono<A> ma = subscribed.once(Mono.just(a0));
    when(dependency.sendMessage(a0)).thenReturn(subscribed.once(Mono.empty()));

    assertThat(runsNonBlocking(() -> workshop.p01_sendMessageAndReturn(ma)).block()).isEqualTo(a0);
  }

  @Test
  void p02_saveSendReturn() {
    when(dependency.save(a0)).thenReturn(subscribed.once(Mono.just(a)));
    when(dependency.sendMessage(a)).thenReturn(subscribed.once(Mono.empty()));

    assertThat(runsNonBlocking(() -> workshop.p02_saveSendReturn(a0)).block()).isEqualTo(a);
  }

  @Test
  void p03_saveSendAuditReturn() {
    when(dependency.save(a0)).thenReturn(subscribed.once(Mono.just(a)));
    when(dependency.sendMessage(a)).thenReturn(subscribed.once(Mono.empty()));
    when(dependency.audit(a)).thenReturn(subscribed.once(Mono.empty()));

    assertThat(runsNonBlocking(() -> workshop.p03_saveSendAuditReturn(a0)).block()).isEqualTo(a);
  }

  @Test
  void p04_saveSendAuditKOReturn() {
    when(dependency.save(a0)).thenReturn(subscribed.once(Mono.just(a)));
    when(dependency.sendMessage(a)).thenReturn(subscribed.once(Mono.empty()));
    when(dependency.audit(a)).thenReturn(subscribed.once(Mono.empty()));

    assertThat(runsNonBlocking(() -> workshop.p04_saveSendAuditKOReturn(a0)).block()).isEqualTo(a);
  }

  @Test
  void p04_saveSendAuditKOReturn_KO() {
    when(dependency.save(a0)).thenReturn(subscribed.once(Mono.just(a)));
    when(dependency.sendMessage(a)).thenReturn(subscribed.once(Mono.error(new IllegalArgumentException())));
    when(dependency.audit(a)).thenReturn(subscribed.once(Mono.empty()));

    assertThat(runsNonBlocking(() -> workshop.p04_saveSendAuditKOReturn(a0)).block()).isEqualTo(a);
  }

  @Test
  @Timeout(1)
  void p05_saveSend_par_AuditReturn() {
    when(dependency.save(a0)).thenReturn(subscribed.once(Mono.just(a)));
    when(dependency.sendMessage(a)).thenReturn(subscribed.once(Mono.delay(ofMillis(600)).then()));
    when(dependency.audit(a)).thenReturn(subscribed.once(Mono.delay(ofMillis(600)).then()));

    assertThat(runsNonBlocking(() -> workshop.p05_saveSend_par_AuditReturn(a0)).block()).isEqualTo(a);
  }

  @Test
  void p06_save_sendFireAndForget() {
    when(dependency.save(a0)).thenReturn(subscribed.once(Mono.just(a)));
    when(dependency.sendMessage(a)).thenReturn(subscribed.once(Mono.empty()));

    assertThat(runsNonBlocking(() -> workshop.p06_save_sendFireAndForget(a0)).block()).isEqualTo(a);
  }

  @Test
  @CaptureSystemOutput
  void p06_save_sendFireAndForget_errorsSilencedButLogged(OutputCapture outputCapture) {
    when(dependency.save(a0)).thenReturn(subscribed.once(Mono.just(a)));
    when(dependency.sendMessage(a)).thenReturn(subscribed.once(Mono.error(new IllegalStateException("TEST ERROR"))));

    assertThat(runsNonBlocking(() -> workshop.p06_save_sendFireAndForget(a0)).block()).isEqualTo(a);
    Utils.sleep(300);
    assertThat(outputCapture.toString()).contains("TEST ERROR");
  }

  @Test
  @Timeout(1)
  void p06_save_sendFireAndForget_notWaited() {
    when(dependency.save(a0)).thenReturn(subscribed.once(Mono.just(a)));
    when(dependency.sendMessage(a)).thenReturn(subscribed.once(Mono.delay(ofMillis(2000)).then()));

    assertThat(runsNonBlocking(() -> workshop.p06_save_sendFireAndForget(a0)).block()).isEqualTo(a);
  }
}
