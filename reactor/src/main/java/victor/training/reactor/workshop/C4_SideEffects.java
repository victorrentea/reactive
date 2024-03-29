package victor.training.reactor.workshop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class C4_SideEffects {
  protected final Logger log = LoggerFactory.getLogger(C4_SideEffects.class);
  protected static class A {
    public boolean updated;
  }
  protected enum AStatus {
    NEW, UPDATED, CONFLICT
  }

  protected interface Dependency {
    Mono<A> save(A a);

    Mono<AStatus> retrieveStatus(A a);

    Mono<Void> sendMessage(A a);

    Mono<Void> audit(A a);
  }

  protected final Dependency dependency;

  public C4_SideEffects(Dependency dependency) {
    this.dependency = dependency;
  }

  // ==================================================================================================

  /**
   * TODO Call dependency.sendMessage(a) when the mono emits an A; then return the same A.
   * Solution#1: .flatMap
   * Solution#2: .delayUntil
   */
  public Mono<A> p01_sendMessageAndReturn(Mono<A> ma) {
    // equivalent blocking⛔️ code:
    A a = ma.block();
    dependency.sendMessage(a).block();
    return Mono.just(a);
  }

  // ==================================================================================================

  /**
   * TODO Call a = .save(a0) then .sendMessage(a) and return only the completion signal
   */
  public Mono<Void> p02_saveAndSend(A a0) {
    // equivalent blocking⛔️ code:
    A a = dependency.save(a0).block();
    dependency.sendMessage(a).block();
    return Mono.empty();
  }

  // ==================================================================================================

  /**
   * TODO Call a = .save(a0);
   *  then, if a.updated => .sendMessage(a)
   */
  public Mono<Void> p03_saveSendIfUpdated(A a0) {
    // equivalent blocking⛔️ code:
    A a = dependency.save(a0).block();
    if (a.updated) {
      dependency.sendMessage(a).block();
    }
    return Mono.empty();
  }

  // ==================================================================================================

  /**
   * TODO Call a = .save(a0);
   *  then, if .retrieveStatus(a) == CONFLICT => .sendMessage(a)
   */
  public Mono<Void> p04_saveSendIfConflictRemote(A a0) {
    // equivalent blocking⛔️ code:
    A a = dependency.save(a0).block();
    if (dependency.retrieveStatus(a).block() == AStatus.CONFLICT) {
      dependency.sendMessage(a).block();
    }
    return Mono.empty();
  }

  // ==================================================================================================

  /**
   * TODO Call a = .save(a0), then .sendMessage(a), then .audit(a)
   */
  public Mono<A> p05_saveSendAuditReturn(A a0) {
    // equivalent blocking⛔️ code:
    A a = dependency.save(a0).block();
    dependency.sendMessage(a).block();
    dependency.audit(a).block();
    return Mono.just(a);
  }

  // ==================================================================================================

  /**
   * TODO same as above but ignore any errors from sendMessage.
   * ! Make sure audit() is called and the returned Mono<> completes without error.
   */
  public Mono<A> p06_ignoreError(A a0) {
    // equivalent blocking⛔️ code:
    A a = dependency.save(a0).block();
    try {
      dependency.sendMessage(a).block();
    } catch (Exception e) {
      log.error("Error sending message: " + e);
    }
    dependency.audit(a).block();
    return Mono.just(a);
  }


  // ==================================================================================================

  /**
   * TODO same as above, but to gain time, sendMessage and audit should happen in parallel.
   * Note: Any error in either save(), send() or audit() should be returned in the returned Mono
   */
  public Mono<A> p07_parallel(A a0) {
    return dependency.save(a0)
            // TODO
            ;
  }

  // ==================================================================================================

  /**
   * TODO a = save(a0); then call sendMessage(a) but don't wait for the sending to COMPLETE.
   *
   * In other words, the returned Mono should complete immediately after save() completes,
   * leaving in background the call to sendMessage (aka 'fire-and-forget').
   *    Why: faster response to user
   *
   * Also, make sure any error from sendMessage is logged in the console.
   *
   * BONUS[HARD]: make sure any data in the Reactor Context is propagated into the sendMessage.
   *  Why?: propagate call metadata like ReactiveSecurityContext, @Transactional, Sleuth traceID, Logback MDC, ..
   */
  public Mono<A> p08_fireAndForget(A a0) {
    return dependency.save(a0)
            // TODO
            ;
  }

}
