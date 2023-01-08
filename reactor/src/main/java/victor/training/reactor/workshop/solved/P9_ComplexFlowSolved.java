package victor.training.reactor.workshop.solved;

import reactor.core.publisher.Mono;
import victor.training.reactor.workshop.P9_ComplexFlow;

public class P9_ComplexFlowSolved extends P9_ComplexFlow {

  public P9_ComplexFlowSolved(Dependency dependency) {
    super(dependency);
  }

  public Mono<Void> p06_complexFlow(int id) {
    return
            // *** parallel
            dependency.d(id)
                .zipWith(
                    dependency.a(id)
                            .flatMap(a -> dependency.b(a).zipWith(dependency.c(a),
                                    (b, c) -> new MyContext().withA(a).withB(b).withC(c))),
                    (d, abc) -> abc.withD(d)
                    )

    // *** SEQUENTIAL
//          Mono.just(new MyContext())
//            .flatMap(context -> dependency.a(id).map(context::withA))
//            .flatMap(context -> dependency.d(id).map(context::withD))
//            .flatMap(context -> dependency.b(context.getA()).map(context::withB))
//            .flatMap(context -> dependency.c(context.getA()).map(context::withC))

            .map(context -> context.withA1(logic(context.getA(), context.getB(), context.getC(), context.getD())))

            .delayUntil(context -> dependency.saveA(context.getA1()))

            .doOnNext(context -> dependency.auditA(context.getA1(), context.getA()).subscribe())
            .then();
  }
}
