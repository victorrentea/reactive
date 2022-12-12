package victor.training.reactor.workshop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static java.time.Duration.ofMillis;

public class P4_SideEffects {
  protected final Logger log = LoggerFactory.getLogger(P4_SideEffects.class);
  static class A {
  }
  enum AStatus {
    NEW, UPDATED, CONFLICT
  }

  interface Dependency {
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
   * TODO Call dependency.sendMessage(a) when the mono emits an A.
   *  Then return the same A further.
   * Solution1: .flatMap
   * Solution2: .delayUntil
   */
  public Mono<A> p01_sendMessageAndReturn(Mono<A> ma) {
    // equivalent blocking⛔️ code:
//    A a = ma.block();
//    dependency.sendMessage(a).block();
//    return Mono.just(a);

 return ma
//            .flatMap(a -> dependency.sendMessage(a).thenReturn(a)) // perfect, doar ca e atat de frecv ca au facut op dedicat:
//            .delayUntil(a -> dependency.sendMessage(a))
            .delayUntil(dependency::sendMessage) // geek version
            ;
  }

  // ==================================================================================================

  /**
   * TODO Call a = .save(a0) then .sendMessage(a) and return only the completion signal
   */
  public Mono<Void> p02_saveAndSend(A a0) {
    // equivalent blocking⛔️ code:
//    A a = dependency.save(a0).block();
//    dependency.sendMessage(a).block();
//    return Mono.empty();
    return dependency.save(a0)
            .flatMap(dependency::sendMessage);

  }

  // ==================================================================================================

  /**
   * TODO Call a = .save(a0);
   *  then, if .retrieveStatus(a) is CONFLICT => .sendMessage(a)
   */
  public Mono<Void> p03_saveSendIfConflict(A a0) {
    // equivalent blocking⛔️ code:
//    A a = dependency.save(a0).block();
//    if (dependency.retrieveStatus(a).block() == AStatus.CONFLICT) {
//      dependency.sendMessage(a).block();
//    }
//    return Mono.empty();

//    return dependency.save(a0)
//            .flatMap(a -> dependency.retrieveStatus(a)
//                    .flatMap(status -> status == AStatus.CONFLICT ? dependency.sendMessage(a) : Mono.empty()
//                    ));

    return dependency.save(a0)
            .filterWhen(a -> dependency.retrieveStatus(a).map(status -> status == AStatus.CONFLICT))
            .flatMap(a->dependency.sendMessage(a));
  }

  // ==================================================================================================

  /**
   * TODO Call a = .save(a0) then .sendMessage(a) and .audit(a) and return the 'a' returned by save
   */
  public Mono<A> p04_saveSendAuditReturn(A a0) {
    return dependency.save(a0)
            .delayUntil(a->Mono.zip(dependency.sendMessage(a), dependency.audit(a)))
            ;
  }

  // ==================================================================================================

  /**
   * TODO same as above but ignore any errors from sendMessage.
   * ! Make sure audit() is called and the returned Mono<> completes without error.
   */
  public Mono<A> p05_saveSendAuditKOReturn(A a0) {
    return dependency.save(a0)
            .delayUntil(a -> dependency.sendMessage(a)
                    .doOnError(e -> log.error(e.getMessage(), e))
                    .onErrorResume(e -> Mono.empty()))
            .delayUntil(dependency::audit);
  }


  // ==================================================================================================

  /**
   * TODO a = .save(a0) then .sendMessage(a) and .audit(a) and return the 'a' returned by save, but
   *  to gain some time, sendMessage and audit should happen in parallel.
   * Note: Any error in either save(), send() or audit() should be returned in the returned Mono
   */
  public Mono<A> p06_saveSend_par_AuditReturn(A a0) {
    return dependency.save(a0)
            // TODO
            ;
  }

  // ==================================================================================================

  /**
   * TODO a = save(a0); then call sendMessage(a); but don't wait for this to complete.
   * In other words, the returned Mono should complete immediately after save() completes with the 'a' returned by it,
   * leaving in the background the call to sendMessage running (aka 'fire and forget').
   * Also, make sure any error from sendMessage is logged in the console.
   *
   * BONUS: make sure any data in the reactor context sent by the subscriber is propagated into the sendMessage reactor context.
   */
  public Mono<A> p07_save_sendFireAndForget(A a0) {
    return dependency.save(a0)

            // il las max 200 ms. altfel ii dau cancel si continui ca si cum nimic nu s-ar fi intamplat
//            .delayUntil(a -> dependency.sendMessage(a).timeout(ofMillis(200), Mono.empty()))

            .doOnNext(a -> dependency.sendMessage(a).subscribe())
            // de ce NU se recomanda sa faci .subscribe() de mana ci sa "chainuiesti" toate operatiile pe care le faci
            // si sa dai un Flux/Mono lui Spring in final, intorcandu-l dintr-o metoda de controller eg @GetMapping

            // 1) sa uiti sa logezi erorile! (by default Reactor logeaza)

            // 2) .cancel() pe subscriptia facuta de mana NU se executa daca Mono<A> returnat din fct asta (lu spring) este cancel.
            //      // no cancellation signal propagation -> in BE nu prea ne doare. In ANdroid doare. FE:
                // 'n-are sens sa mai lasi apelurile http la BE sa mearga daca omu a inchis pagina/applicatia)

            // 3) Reactor Context propagation is blocked. metadatele asociate req curent (AT, userid, tenantId, @Transactional, MDC)
            // NU se mai propaga pe sendMessage()

            ;
  }

}
