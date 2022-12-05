package victor.training.reactor.workshop;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class SideEffects {
  protected final Logger log = LoggerFactory.getLogger(SideEffects.class);
  static class A {
  }

  interface Dependency {
    Mono<A> save(A a);

    Mono<Void> sendMessage(A a);

    Mono<Void> audit(A a);
  }

  protected final Dependency dependency;

  public SideEffects(Dependency dependency) {
    this.dependency = dependency;
  }

  // ==================================================================================================

  /**
   * Call dependency.sendMessage(a) when the mono emits an A.
   * Then return the same A further.
   * Solution1: .flatMap
   * Solution2: .delayUntil
   */
  public Mono<A> p01_sendMessageAndReturn(Mono<A> ma) {
    // equivalent blocking⛔️ code:
//    A a = ma.block();
//    dependency.sendMessage(a).block();
//    return Mono.just(a);
//    return ma.flatMap(a-> dependency.sendMessage(a).thenReturn(a));
//    return ma.delayUntil(dependency::sendMessage);
    return ma.delayUntil(a-> dependency.sendMessage(a));
  }

  // ==================================================================================================

  /**
   * Call a = .save(a0) then .sendMessage(a) and return the 'a' returned by save
   */
  public Mono<A> p02_saveSendReturn(A a0) {
    // equivalent blocking⛔️ code:
//    A a = dependency.save(a0).block();
//    dependency.sendMessage(a).block();
//    return Mono.just(a);
//    dependency.sendMessage(a)
    return dependency.save(a0).delayUntil(a -> dependency.sendMessage(a));
  }

  // ==================================================================================================

  /**
   * Call a = .save(a0) then .sendMessage(a) and .audit(a) and return the 'a' returned by save
   */
  public Mono<A> p03_saveSendAuditReturn(A a0) {
//    // equivalent blocking⛔️ code:
//    A a = dependency.save(a0).block();
//    dependency.sendMessage(a).block();
//    dependency.audit(a).block();
//    return Mono.just(a); -javaagent:blockhound.jar pe pre-prod in care mirroruiau requesturile din prod sa vanezi blochezi
    return dependency.save(a0)
            .delayUntil(a ->

                    dependency.sendMessage(a).thenReturn("toAvoidCancellingTheOther")
                    .zipWith(dependency.audit(a).thenReturn("toAvoidCancellingTheOther")));
  }

  // ==================================================================================================

  /**
   * same as above but ignore any errors from sendMessage.
   * Make sure audit() is called and the returned Mono<> completes without error.
   */
  public Mono<A> p04_saveSendAuditKOReturn(A a0) {
    // equivalent blocking⛔️ code:
//    A a = dependency.save(a0).block();
//    try {
//      dependency.sendMessage(a).block();
//    } catch (Exception e) {
//      // shaworma pattern - inghiti cu de toate
//    }
//    dependency.audit(a).block();
//    return Mono.just(a);
    return dependency.save(a0)
            .delayUntil(a -> dependency.sendMessage(a).onErrorResume(e -> Mono.empty()))
            .delayUntil(a -> dependency.audit(a));
  }


  // ==================================================================================================

  /**
   * all a = .save(a0) then .sendMessage(a) and .audit(a) and return the 'a' returned by save, but
   * to gain some time, sendMessage and audit should happen in parallel.
   * An error in either save(), send() or audit() should be returned in the returned Mono
   */
  public Mono<A> p05_saveSend_par_AuditReturn(A a0) {
    return dependency.save(a0)
            // TODO
            ;
  }

  // ==================================================================================================

  /**
   * a = save(a0); then call sendMessage(a); but don't wait for this to complete.
   * In other words, the returned Mono should complete immediately after save() completes with the 'a' returned by it,
   * leaving in the background the call to sendMessage running (aka 'fire and forget').
   * Extra: Make sure any error from sendMessage is logged in the console.
   */
  public Mono<A> p06_save_sendFireAndForget(A a0) {
    return dependency.save(a0)
            .doOnNext(a -> dependency.sendMessage(a)
                    .subscribe(v->{/*callback*/}, e->log.error("Valeu "+ e,e)))
            // de ce .subscribe() e rau? de ce galben in IDEA?
            // 1) promoveaza un stil de coding bazat pe callbackuri => multe side effects = rau! nu FP
            // Functional Reactive Progamming : doar imutabile, cat mai putine side effects, pure functions toate.

            // 2) NU se propaga .cancel signal de la Mono<A> returnat de functie. = rar util in practica in BE
            ;
    // best practice NU subscrii tu niciodata, ci dai mai departe (cui?)  Springului. EL va subscrie, nu tu.

    // Exista doar 2-3 cazuri in care NU poti da lui Spring sa subscrie la fluxul tau
    // 1) @Scheduled <- nu prea mai e la moda in cloud elastic envs
    // 2) consumi mesaje de pe cozi
        // ===> in aceste 2 cazuri e ok sa faci .block()!
  }
}
