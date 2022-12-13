package victor.training.reactor.workshop;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaTypeEditor;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;

import java.util.Optional;

import static reactor.function.TupleUtils.function;

@Slf4j
public class P2_Enrich {
  static class A {
  }

  static class B {
  }

  static class C {
    boolean reject;
  }

  static class D {
  }

  @Value
  @With
  static class AB {
    public A a;
    public B b;
  }

  @Value
  static class ABC {
    public A a;
    public B b;
    public C c;
  }

  interface Dependency {
    Mono<A> a(int id);

    Mono<B> b(int id);

    Mono<B> b1(A a);

    Mono<C> c(int id);

    Mono<C> c1(A a);

    Mono<C> c2(A a, B b);

    Mono<D> d(int id);

    Mono<A> saveA(A a);

    Mono<Void> auditA(A a1, A a0);
  }

  protected final Dependency dependency;

  public P2_Enrich(Dependency dependency) {
    this.dependency = dependency;
  }

  //  😇 😇 😇 😇 😇 😇 😇 😇 😇 😇 😇 😇 😇 😇 😇 😇 😇 😇 😇 😇 😇
  // ⚠️ ATTENTION ⚠️ ENTERING REACTIVE HEAVEN ⚠️
  //
  // * ALL THE NETWORK CALLS HAVE BEEN CAREFULLY WRAPPED IN NON-BLOCKING FUNCTIONS
  //   eg. relying on WebClient for REST calls
  //   and reactive drivers for SQL (R2DBC), Mongo, Kafka, Redis, Cassandra, ..
  // * NO FUNCTION EVER BLOCKS THE CALLER THREAD ANYMORE
  // * NO FUNCTION RETURNING MONO THROWS EXCEPTIONS (=> Mono.error(Ex))
  // !! CAREFUL NOT TO SUBSCRIBE TWICE TO THE RETURN Publishers
  // ********************************


  // ==================================================================================================

  /**
   * Call a(id) and b(id) with the given id, then call AB(a,b) with the results retrieved.
   * a(id) || b(id) ==> AB(a,b)
   * Hint: .zip
   */
  public Mono<AB> p01_a_par_b(int id) {
    // equivalent blocking⛔️ code:
    //         A a = dependency.a(id).block();
    //         B b = dependency.b(id).block();
    //         return Mono.just(new AB(a, b));

    return dependency.a(id)
            .zipWith(dependency.b(id),
                    (a, b) -> new AB(a, b));
  }

  // ==================================================================================================

  /**
   * a(id) || b(id) || c(id) ==> ABC(a,b,c)
   */
  public Mono<ABC> p02_a_b_c(int id) {
    // equivalent blocking⛔️ code:
    //A a = dependency.a(id).block();
    //B b = dependency.b(id).block();
    //C c = dependency.c(id).block();
    //return Mono.just(new ABC(a, b, c));

    // Hint: use Mono.zip (static method)
    // Hint: avoid tuple -> {} by using TupleUtils.function((a,b,c) -> {})
    return Mono.zip(
                    dependency.a(id),
                    dependency.b(id),
                    dependency.c(id)
            )
            // impuscam o musca cu o bazooka: flat Map e folosit pentru a chaineui ALT APEL DE RETEA.
            // in lambda chemi o fct ce face retea.
            //                .flatMap(tuple3 -> Mono.just(new ABC(tuple3.getT1(), tuple3.getT2(), tuple3.getT3())))

            // are voie sa execute doar transformate in memorie (fara retea)
            //                .map(tuple3 -> new ABC(tuple3.getT1(), tuple3.getT2(), tuple3.getT3()))
            .map(function((a, b, c) -> new ABC(a, b, c)))
            //                .map(TupleUtils.function(ABC::new)) // ca sa nu mai inteleaga nimeni!! - Job Security

            ;
  }

  // ==================================================================================================

  /**
   * Call a(id) with the given parameter, then b1(a) with the retrieved a;
   * return both a and b.
   * a(id), then b1(a) ==> AB(a,b)
   */
  public Mono<AB> p03_a_then_b1(int id) {
    // equivalent blocking⛔️ code:
    // A a = dependency.a(id).block();
    // B b = dependency.b1(a).block();
    // return Mono.just(new AB(a, b));

    // Hint: Mono#flatMap
      return dependency.a(id)
//              .flatMap(a -> dependency.b1(a).map(b -> new AB(a, b)))
//              .zipWhen(a -> dependency.b1(a), (a, b) -> new AB(a,b))
              .zipWhen(dependency::b1, AB::new)
              ;
  }

  // ==================================================================================================

  /**
   * a(id), then b1(a) || c1(a) ==> ABC(a,b,c)
   * Relax, you are in Heaven😇: calling b1 and c1 return immediately, without blocking.
   */
  public Mono<ABC> p04_a_then_b1_c1(int id) {
    // Hint mono.flatMap(->mono.zipWith(mono, ->))
     return dependency.a(id)
            .flatMap(a->dependency.b1(a).zipWith(dependency.c1(a), (b,c) -> new ABC(a,b,c)  ));
  }

  // ==================================================================================================

  /**
   * Same problem as above but this time define multiple Mono<> variables.
   * !! Use Mono#cache to avoid repeating the call to a(id))
   * a(id), then b1(a) || c1(a) ==> ABC(a,b,c)
   * Relax, you are in Heaven😇: calling b1 and c1 return immediately, without blocking.
   */
  public Mono<ABC> p04_a_then_b1_c1_cache(int id) {
    Mono<A> ma = dependency.a(id)
            .metrics() // afiseaza pe Grafana de cate ori te-ai subscris si cat a durat sa vina elementul
            .doOnSubscribe(s-> System.out.println("ACUM PLEACA CALLU DE RETEA"))
            .cache()

            ;
    // there are only 2 things hard in programming:
    // - cache invalidation> valeu: cacheul asta cat traieste?
        // doar cat traieste in heap instanta de Mono
        // cu alte cuvinte, doar pentru instanta asta de flux
    // - naming things

    // fara .cache -> bug=ma subscriam de 3 ori la Mono<A> => 3 NETWORK CALLS.
    Mono<B> mb = ma.flatMap(a-> dependency.b1(a));
    Mono<C> mc = ma.flatMap(a-> dependency.c1(a));
    return Mono.zip(ma,mb,mc)
            .map(function((a, b, c)->new ABC(a,b,c)))
            ;


    // Concluzie coding practice: NU AI VOIE SA DECLARI VARIABILE Mono/Flux
    // efect : vei vedea metode de 10-20 de linii ce inlantuie un reactive chain
          //NU:  Exista si dirty hack: .cache() < nu o folosi. pt ca degenereaza

    // Cum scriu un test sa pice pe cazu asta
  }


  // ==================================================================================================

  /**
   * a(id), then b1(a), then c2(a,b) ==> ABC(a,b,c)
   */
  public Mono<ABC> p05_a_then_b1_then_c2(int id) {
    // equivalent blocking⛔️ code:
//    A a = dependency.a(id).block();
//    B b = dependency.b1(a).block();
//    C c = dependency.c2(a, b).block();
//    return Mono.just(new ABC(a, b, c));



    // TODO Solution #1: accumulating intermediary data structures (chained flatMap)
    return dependency.a(id)
            .zipWhen(dependency::b1)
            .zipWhen(TupleUtils.function(dependency::c2),
                    (tab, c) -> new ABC(tab.getT1(), tab.getT2(), c));


    // TODO Solution #2 (geek): nested flatMap
//    return dependency.a(id)
//            .flatMap(a -> dependency.b1(a)
//                    .flatMap(b -> dependency.c2(a,b)
//                            .map(c->new ABC(a,b,c))
//                    )
//            );

    // TODO Solution #3 (risky): cached Mono<>
    //      eg Mono<A> ma = dependency.a(id).cache();
  }

  // ==================================================================================================

  /**
   * a(id) then b1(a) ==> AB(a,b),
   *  but if b1(id) returns empty() => AB(a,null)
   * Hint: watch out not to lose the data signals.
   * Challenge is that Flux/Mono cannot carry a "null" data signal.
   * Hint: you might need an operator containing "empty" in its name
   */
  public Mono<AB> p06_a_then_bMaybe(int id) {
    return dependency.a(id)
//            .zipWhen(a -> dependency.b1(a).defaultIfEmpty(null), AB::new); // prima idee, dar nu poti da null ca valoare in Reactor

//            .zipWhen(a -> dependency.b1(a)
//                                    .map(Optional::of)
//                                    .defaultIfEmpty(Optional.empty())
//                    , (a1, bOpt) -> new AB(a1, bOpt.orElse(null)));

            .flatMap(a -> dependency.b1(a)
                    .map(b -> new AB(a, b))
                    .defaultIfEmpty(new AB(a,null))
            );
  }
  // ==================================================================================================

  /**
   * a(id) || b(id) ==> AB(a,b), but if b(id) returns empty() => AB(a,null)
   * Finish the flow as fast as possible by starting a() in parallel with b()
   * Challenge is that Flux/Mono cannot carry a "null" data signal.
   * Hint: watch out not to lose the data signals.
   */
  public Mono<AB> p07_a_par_bMaybe(int id) {
    return Mono.zip(dependency.a(id),
            dependency.b(id).map(Optional::of).defaultIfEmpty(Optional.empty()),
            (a, bOpt) -> new AB(a, bOpt.orElse(null)));
  }

  // ==================================================================================================

  /**
   * a(id) || b(id) ==> AB(a,b), but if b(id) fails (Mono.error) => AB(a,null)
   * Hint: watch out not to lose the data signals.
   */
  public Mono<AB> p08_a_try_b(int id) {
    return Mono.zip(dependency.a(id),
            dependency.b(id)
                    .map(Optional::of)
                    .onErrorReturn(Optional.empty())
                    .defaultIfEmpty(Optional.empty()),
            (a, bOpt) -> new AB(a, bOpt.orElse(null)));
  }

  // ==================================================================================================

  /**
   * === UseCase Context Pattern ===
   * The moral of the above example is to avoid method variables completely.
   * a(id), then b1(a), then c2(a,b); also d(id) ==> P10UseCaseContext(a,b,c,d)
   */
  @Value // imutable object
  @With // public P10UseCaseContext withA(A newa) { return new P10UseCaseContext(newa, b,c,d); }
  @AllArgsConstructor
  static class P10UseCaseContext {
    int id;
    A a;
    B b;
    C c;
    D d;

    public P10UseCaseContext(int id) { // initial UC parameters
      this(id, null, null, null, null);
    }
  }

  public Mono<P10UseCaseContext> p10_contextPattern(int id) {
    return Mono.just(new P10UseCaseContext(id))
            .zipWhen(context -> dependency.a(context.id), P10UseCaseContext::withA)
            .zipWhen(context -> dependency.b1(context.a), P10UseCaseContext::withB)
            .zipWhen(context -> dependency.c2(context.a, context.b), P10UseCaseContext::withC)

            // slight inefficiency: de ce astept sa-l cer pe B pana am facut si a,b,c ??? b e independent de ele.
            .zipWhen(context -> dependency.d(context.id), P10UseCaseContext::withD)
            ;
  }

}
