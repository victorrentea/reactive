package victor.training.reactor.workshop;

import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;

import java.util.function.Function;

public class EnrichSolved extends Enrich {
    public EnrichSolved(Dependency dependency) {
        super(dependency);
    }

    public Mono<AB> p01_a_par_b(int id) {
        return dependency.a(id).zipWith(dependency.b(id), AB::new);
    }

    public Mono<ABC> p02_a_b_c(int id) {
        Mono<A> ma = dependency.a(id);
        Mono<B> mb = dependency.b(id);
        Mono<C> mc = dependency.c(id);
        return Mono.zip(ma, mb, mc)
                .map(TupleUtils.function((a, b, c) -> new ABC(a, b, c)));
    }

    public Mono<AB> p03_a_then_b1(int id) {
        return dependency.a(id).flatMap(a -> dependency.b1(a).map(b -> new AB(a,b)));
    }

    public Mono<ABC> p04_a_then_b1_c1(int id) {
        return dependency.a(id)
                .flatMap(a -> dependency.b1(a).zipWith(dependency.c1(a),
                            (b,c) -> new ABC(a,b,c)));
    }

    public Mono<ABC> p04_a_then_b1_c1_cache(int id) {
        Mono<A> ma = dependency.a(id).cache();
        Mono<B> mb = ma.flatMap(a -> dependency.b1(a));
        Mono<C> mc = ma.flatMap(a -> dependency.c1(a));
        return Mono.zip(ma, mb, mc).map(TupleUtils.function((a, b, c) -> new ABC(a, b, c)));
    }

    public Mono<ABC> p05_a_then_b1_then_c2(int id) {
        // Solution #1: accumulating data structures (chained flatMap)
        //return dependency.a(id)
        //        .flatMap(a -> dependency.b1(a)
        //                .map(b -> new AB(a, b)))
        //        .flatMap(ab -> dependency.c2(ab.a, ab.b)
        //                .map(c -> new ABC(ab.a, ab.b, c)));

        // Solution #2 (geek): nested flatMap
        //return dependency.a(id)
        //    .flatMap(a -> dependency.b1(a).flatMap(
        //            b -> dependency.c2(a, b).map(
        //                    c -> new ABC(a, b, c))));

        // Solution #3 (imperative-style but risky): .cache()d Mono<> variables
        Mono<A> ma = dependency.a(id).cache();
        Mono<B> mb = ma.flatMap(a -> dependency.b1(a)).cache();
        Mono<C> mc = ma.zipWith(mb, (a,b) -> dependency.c2(a, b)).flatMap(Function.identity());
        return Mono.zip(ma, mb, mc).map(TupleUtils.function((a, b, c) -> new ABC(a, b, c)));
    }


    public Mono<P6UseCaseContext> p06_context(int id) {
        return dependency.a(id).zipWith(dependency.d(id),
                        (a, d) -> new P6UseCaseContext(id).withA(a).withD(d))
                .flatMap(context -> dependency.b1(context.getA()).map(context::withB))
                .flatMap(context -> dependency.c2(context.getA(), context.getB()).map(context::withC));
    }
}
