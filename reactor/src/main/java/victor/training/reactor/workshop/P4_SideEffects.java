package victor.training.reactor.workshop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class P4_SideEffects {
  protected final Logger log = LoggerFactory.getLogger(P4_SideEffects.class);
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

  public P4_SideEffects(Dependency dependency) {
    this.dependency = dependency;
  }

  // ==================================================================================================

  /**
   * TODO Call dependency.sendMessage(a) when the mono emits an A; then return the same A.
   * Solution#1: .flatMap
   * Solution#2: .delayUntil
   */
  public Mono<A> p01_sendMessageAndReturn(Mono<A> ma) {
//    return ma.doOnNext(a -> dependency.sendMessage(a)); // NOthing happes
//    return ma.doOnNext(a -> dependency.sendMessage(a).subscribe()); // NO-bad practice
        // because it breaks the reactive chaing
    // so what ??
        // 1) the hidden dark metadata does not propaget to sendMessage:
          // TraceID, @Transactional, ReactiveSecurityContextHolder,
        // 2) if the client of the Mono<A> that i returned CANCELS the request,
          // the sendMessage is NOT cancelled

        // What do do instead: when using Reactive make sure you return the Mono/Flux to Spring
    // eg from a @GetMappng method. ...

//    return ma.flatMap(a -> dependency.sendMessage(a).map(v -> a));
    return ma.flatMap(a -> dependency.sendMessage(a).thenReturn(a));
//    return ma.delayUntil(a -> dependency.sendMessage(a));
//    return ma.delayUntil(dependency::sendMessage);
  }

  // ==================================================================================================

  /**
   * TODO Call a = .save(a0) then .sendMessage(a) and return only the completion signal
   */
  public Mono<Void> p02_saveAndSend(A a0) {
    return dependency.save(a0)
            .flatMap(a -> dependency.sendMessage(a));
  }

  // ==================================================================================================

  /**
   * TODO Call a = .save(a0);
   *  then, if .retrieveStatus(a) == CONFLICT => .sendMessage(a)
   */
  public Mono<Void> p03_saveSendIfConflict(A a0) {
    // equivalent blocking⛔️ code:
//    A a = dependency.save(a0).block();
//    if (dependency.retrieveStatus(a).block() == AStatus.CONFLICT) {
//      dependency.sendMessage(a).block();
//    }

    return dependency.save(a0)
            .filterWhen(a -> dependency.retrieveStatus(a).map(s -> s == AStatus.CONFLICT))
            .flatMap(a -> dependency.sendMessage(a));
  }

  // ==================================================================================================

  /**
   * TODO Call a = .save(a0)
   *  if (a.updated) then .sendMessage(a) and .audit(a)
   */
  public Mono<Void> p04_saveSendAuditReturn(A a0) {
    // equivalent blocking⛔️ code:
    return dependency.save(a0)
            .filter(a -> a.updated)
            .flatMap(a -> Mono.zip(dependency.sendMessage(a), dependency.audit(a)).then());
  }

  // ==================================================================================================

  /**
   * TODO same as above but ignore any errors from sendMessage.
   * ! Make sure audit() is called and the returned Mono<> completes without error.
   */
  public Mono<A> p05_saveSendAuditKOReturn(A a0) {
    // equivalent blocking⛔️ code:
//    A a = dependency.save(a0).block();
//    try {
//      dependency.sendMessage(a).block();
//    } catch (Exception e) {
//      log.error("Error sending message: " + e);
//    }
//    dependency.audit(a).block();
////    return Mono.just(a);

    return dependency.save(a0)
            .delayUntil(a->
                  Mono.zip(
                    dependency.sendMessage(a)
                            .doOnError(e-> System.out.println("Error sending message: " + e))
                            .onErrorResume(e -> Mono.empty()),
                    dependency.audit(a))
                  .then());
  }


  // ==================================================================================================

  /**
   * TODO a = save(a0) then sendMessage(a) and audit(a); return a.
   *  BUT: to gain time, sendMessage and audit should happen in parallel.
   * Note: Any error in either save(), send() or audit() should be returned in the returned Mono
   */
  public Mono<A> p06_saveSend_par_AuditReturn(A a0) {
    return dependency.save(a0)
            // TODO
            ;
  }

  // ==================================================================================================

  /**
   * TODO a = save(a0); then call sendMessage(a) but don't wait for this to complete.
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
  public Mono<A> p07_save_sendFireAndForget(A a0) {
    return dependency.save(a0)
            // TODO
            ;
  }

}
