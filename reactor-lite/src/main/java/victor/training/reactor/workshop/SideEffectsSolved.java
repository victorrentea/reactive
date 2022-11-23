package victor.training.reactor.workshop;

import reactor.core.publisher.Mono;

public class SideEffectsSolved extends SideEffects {
  public SideEffectsSolved(Dependency dependency) {
    super(dependency);
  }

  @Override
  public Mono<A> p01_sendMessageAndReturn(Mono<A> ma) {
    // return ma.flatMap(a -> dependency.sendMessage(a).map(v -> a));

    // better:
    return ma.delayUntil(dependency::sendMessage);
  }

  @Override
  public Mono<A> p02_saveSendReturn(A a0) {
    return dependency.save(a0)
            .delayUntil(dependency::sendMessage);
  }

  @Override
  public Mono<A> p03_saveSendAuditReturn(A a0) {
    return dependency.save(a0)
            .delayUntil(dependency::sendMessage)
            .delayUntil(dependency::audit);
  }

  @Override
  public Mono<A> p04_saveSendAuditKOReturn(A a0) {
    return dependency.save(a0)
            .delayUntil(a -> dependency.sendMessage(a).onErrorResume(e -> Mono.empty()))
            .delayUntil(dependency::audit);
  }

  @Override
  public Mono<A> p05_saveSend_par_AuditReturn(A a0) {
    return dependency.save(a0)
            .delayUntil(a -> Mono.zip(dependency.sendMessage(a), dependency.audit(a)));
  }

  @Override
  public Mono<A> p06_save_sendFireAndForget(A a0) {
    return dependency.save(a0)
            .doOnNext(a -> dependency.sendMessage(a).subscribe(v -> {
            }, e -> log.error("Error: " + e)));
  }
}
