package victor.training.reactor.workshop.solved;

import reactor.core.publisher.Mono;
import victor.training.reactor.workshop.C9_ComplexFlow;

public class C9_ComplexFlowSolved extends C9_ComplexFlow {

  public C9_ComplexFlowSolved(Dependency dependency) {
    super(dependency);
  }

  public Mono<Void> p06_complexFlow(int id) {
    return
        dependency.d(id)
            .zipWith(
                dependency.a(id)
                    .flatMap(a -> dependency.b(a).zipWith(dependency.c(a),
                        (b, c) -> new MyContext().withA(a).withB(b).withC(c))),
                (d, abc) -> abc.withD(d)
            )

            .map(context -> context.withA1(logic(context.a(), context.b(), context.c(), context.d())))

            .delayUntil(context -> dependency.saveA(context.a1()))

            .doOnNext(context -> dependency.auditA(context.a1(), context.a())
                .doOnError(e-> log.error("Error: {}", String.valueOf(e)))
                .subscribe())
            .then();
  }
}
