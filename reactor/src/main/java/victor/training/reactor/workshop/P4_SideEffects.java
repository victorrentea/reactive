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
//    return ma.flatMap(a -> dependency.sendMessage(a).thenReturn(a));
//    return ma.delayUntil(a -> dependency.sendMessage(a));
    return ma.delayUntil(dependency::sendMessage);
  }
  //IntelliJ ar fi stiut sa iti dea refactor suggestion de la flatMap la zipWhen? NU.
  //doar ca daca poti implementa o asa sugestie:
  //a) IntelliJ plugin
  //b) refaster https://errorprone.info/docs/refaster + https://www.youtube.com/watch?v=KPNimQMH0k4
  // ==================================================================================================

  /**
   * TODO Call a = .save(a0) then .sendMessage(a) and return only the completion signal
   */
  public Mono<Void> p02_saveAndSend(A a0) {
//    return dependency.save(a0).flatMap(a -> dependency.sendMessage(a).then());
//    return dependency.save(a0).delayUntil(a -> dependency.sendMessage(a)).then();
    return dependency.save(a0)
        .delayUntil(dependency::sendMessage)
        .then();
  }

  // ==================================================================================================
//    if (dependency.retrieveStatus(a).block() == AStatus.CONFLICT) {
//      dependency.sendMessage(a).block();
//    }

  /**
   * TODO Call a = .save(a0);
   *  then, if .retrieveStatus(a) == CONFLICT => .sendMessage(a)
   */
  public Mono<Void> p03_saveSendIfConflict(A a0) {
//    A a = dependency.save(a0).block();
//    if (a.updated) {
//        dependency.sendMessage(a).block();
//    }
//            repo.findIdByName(name).... Mono<boolean>
//    return Mono.empty();
    return dependency.save(a0)
//        .filter(a -> a.updated) // in-memory filtering nu IO => .filter
        .filterWhen(a -> // daca atingi retea
            dependency.retrieveStatus(a).map(status -> status == AStatus.CONFLICT)
            // tre sa intorc un Mono<Boolean>
        )
        .flatMap(a -> dependency.sendMessage(a));
//        .doOnNext(a -> dependency.sendMessage(a)); // NU se trimite mesaj pt ca nu subscrie nimeni la sendMessage Mono

  }

  // ==================================================================================================

  /**
   * TODO Call a = .save(a0)
   *  if (a.updated) then .sendMessage(a) and .audit(a)
   */
  public Mono<Void> p04_saveSendAuditReturn(A a0) {
//    A a = dependency.save(a0).block();
//    if (a.updated) {
//      dependency.sendMessage(a).block();
//      dependency.audit(a).block();
//    }
//    return Mono.empty();
    return dependency.save(a0)
        .filter(a -> a.updated)
        .delayUntil(a -> dependency.sendMessage(a))
//        .doOnNext(a -> {
//          Mono<Void> voidMono = dependency.sendMessage(a);
//        })
        .flatMap(a -> dependency.audit(a));
  }



  // ==================================================================================================

  /**
   * TODO same as above but ignore any errors from sendMessage.
   * ! Make sure audit() is called and the returned Mono<> completes without error.
   */
  public Mono<A> p05_saveSendAuditKOReturn(A a0) {
    return dependency.save(a0)
        .filter(a -> a.updated)
        .delayUntil(a -> dependency.sendMessage(a)
            .doOnError(e->log.error("Audit: ", e))
            .then(Mono.empty()))
        .delayUntil(a -> dependency.audit(a));

//    try {
//    } catch (Exce) {
//      dependency.audit(a) //2
//    }
//    dependency.audit(a) //1
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
//        .doOnNext(a -> dependency.sendMessage(a).subscribe())
        // sa faci .subscribe in cod de prod este rau pt ca:
        // 1) pierzi Reactor Context in care vin metadate de request 😱: TraceId, SecuritContext, MDC - fixabila, dar urat
        // 2) daca subscriberul TAU (care cheama p07_save_sendFireAndForget) da CANCEL, sendMessage s-a dus
        // 3) exceptiile in sendMessage nu le vezi
        .delayUntil(a-> Mono.deferContextual(contextView -> {
          dependency.sendMessage(a).contextWrite(contextView).subscribe();
          return Mono.empty();
        }))
        // echivalent: apelul unei metode @Async, sau CompletableFuture.runAsync(->)
        ;
  }

}
