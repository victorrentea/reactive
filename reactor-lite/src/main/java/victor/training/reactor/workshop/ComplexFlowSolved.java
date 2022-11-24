package victor.training.reactor.workshop;

import reactor.core.publisher.Mono;

public class ComplexFlowSolved extends ComplexFlow {

  public ComplexFlowSolved(Dependency dependency) {
    super(dependency);
  }

  public Mono<Void> p06_complexFlow(int id) {
    // *** sequential
    // .thenCompose(context -> dependency.b(context.a).thenApply(context::withB)
    // .thenCompose(context -> dependency.c(context.a).thenApply(context::withC))
    return
            // *** parallel
            dependency.d(id).zipWith(
                    dependency.a(id)
                            .flatMap(a -> dependency.b(a).zipWith(dependency.c(a), (b, c) -> new MyContext().withA(a).withB(b).withC(c))),
                    (d, abc) -> abc.withD(d)
                    )
            .map(context -> context.withA1(logic(context.getA(), context.getB(), context.getC(), context.getD())))

            .delayUntil(context -> dependency.saveA(context.getA1()))

            .doOnNext(context -> dependency.auditA(context.getA1(), context.getA()).subscribe())
            .then();
  }
}
