package victor.training.reactor.workshop;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static java.util.Optional.ofNullable;

@Slf4j
public class Enrich {
  static class A {
  }

  static class B {
    public static final B EMPTY = new B(); // o instanta speciala care inseamna NIMIC
  }

  static class C {
  }

  static class D {
  }

  @Value
  @With
  static class AB {
    public A a;
    public B b;
  }

  @Value // mai bun de cat @Data: ca face si campurile finale (fara setteri)
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

  public Enrich(Dependency dependency) {
    this.dependency = dependency;
  }

  //  😇 😇 😇 😇 😇 😇 😇 😇 😇 😇 😇 😇 😇 😇 😇 😇 😇 😇 😇 😇 😇
  // ⚠️ ATTENTION ⚠️ ENTERING REACTIVE HEAVEN ⚠️
  //
  // * ALL THE NETWORK CALLS HAVE BEEN CAREFULLY WRAPPED IN NON-BLOCKING FUNCTIONS
  //   eg. relying on WebClient for REST calls
  //   and reactive drivers for SQL (R2DBC), Mongo, Kafka, Redis, Cassandra, ..
  // * NO FUNCTION EVER BLOCKS THE CALLER THREAD ANYMORE !!! orice functie chemi returneaza in 0 ms
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
    // A a = dependency.a(id).block();
    // B b = dependency.b(id).block();
    // return Mono.just(new AB(a, b));

    //        return Mono.zip(dependency.a(id), dependency.b(id))

    // folosim Mono cand functia chemata (lambda)implica networking (IO) - blocheaza
    //                .flatMap(tuple2 -> Mono.just(new AB(tuple2.getT1(), tuple2.getT2())))

    // folosim map cand functia chemata se face in memorie instant
    //                .map(tuple2 -> new AB(tuple2.getT1(), tuple2.getT2()))
    //                ;

    //    return Mono.zip(dependency.a(id)
    //                            .doOnSubscribe(s -> log.info("ACUM LANSEZ a()"))
    //                            // SURSA la >50% din bugurile din reactive chains este ca
    //                            // subscrierea se intampla sau niciodata sau repetat
    //
    //            // FAPTUL ca tu chemi o functie care iti da un Mono/Flux,
    //            // NU INSEAMNA ca a si inceput executia pe retea a acelui call.
    //            // nici o crerere nu se trimite pe retea pana cand cineva nu face subscribe()!!!
    //
    //            // PRIN CONTRAST, CompletableFuture.supplyAsync(() -> {} ), odata ce a fost creat, a si inceput executia
    //                            .log("a"),
    //                    dependency.b(id)
    //                            .log("b")
    //                            .doOnSubscribe(s -> log.info("ACUM LANSEZ b()")),
    //                    (a, b) -> new AB(a, b))
    //            .log("ab")
    //            .doOnSubscribe(s -> log.info("ACUM LANSEZ ab()"));

    //       Mono.zip(dependency.a(id), dependency.b(id), (a, b) -> new AB(a, b));
    Mono<B> mono = dependency.b(id);
    return dependency.a(id)
            //.doOnSubscribe(s->log.info("A"))
            .zipWith(mono // zipWIth se va subscrie la instanta de Mono primita parametru

                    //.doOnSubscribe(s->log.info("B"))
                    ,
                    (a, b) -> new AB(a, b))
            // orice operator este
            // si Subscriber(in sus,catre cine are datele)
            // si Publisher (in jos, cine VREA datele)
            .doOnSubscribe(s -> log.info("AB"));
  }


  // ==================================================================================================

  /**
   * a(id) || b(id) || c(id) ==> ABC(a,b,c)
   */
  // asa NU: Mono<Tuple3<Long, String, List<Tuple2<String,Integer>>>>
  public Mono<ABC> p02_a_b_c(int id) {
    // equivalent blocking⛔️ code:
    //    A a = dependency.a(id).block();
    //    B b = dependency.b(id).block();
    //    C c = dependency.c(id).block();
    //    return Mono.just(new ABC(a, b, c));

    // Hint: use Mono.zip (static saveAudit)
    // Hint: avoid tuple -> {} by using TupleUtils.function((a,b,c) -> {})

    // Sfat din batrani: nu da afara din casa (functie) Tuple-uri ca sperii clientii
    return Mono.zip(
                    dependency.a(id),
                    dependency.b(id),
                    dependency.c(id))
            //            .map(t3 -> new ABC(t3.getT1(), t3.getT2(), t3.getT3())) // 🤢 scarBOSS
            //            .map(TupleUtils.function((a,b,c) -> new ABC(a,b,c)))
            .map(TupleUtils.function(ABC::new))
            ;
  }

  // ==================================================================================================

  /**
   * Call a(id) with the given parameter, then b1(a) with the retrieved a; return both a and b.
   * a(id), then b1(a) ==> AB(a,b)
   */
  public Mono<AB> p03_a_then_b1(int id) {
    // equivalent blocking⛔️ code:
    //     A a = dependency.a(id).block();
    //     B b = dependency.b1(a).block();
    //     return Mono.just(new AB(a, b));

    // Hint: Mono#flatMap
    return dependency.a(id)
            //            .flatMap(a -> dependency.b1(a) .map(b -> new AB(a,b))  )
            //             .zipWhen(dependency::b1, AB::new) // rupe-i zice IDEA
            .zipWhen(a -> dependency.b1(a), (a, b) -> new AB(a, b)) // hai ca am trait, un op dedicat pt un flux uzual
            ;
  }

  // ==================================================================================================

  /**
   * a(id), then b1(a) in parallel cu c1(a) ==> ABC(a,b,c)
   * Relax, you are in Heaven😇: calling b1 and c1 return immediately, without blocking.
   */
  public Mono<ABC> p04_a_then_b1_c1(int id) {
    // Hint mono.flatMap(->mono.zipWith(mono, ->))
    //    return dependency.a(id)
    //            .zipWhen(a -> dependency.b1(a).zipWith(dependency.c1(a)))
    //            .map(t -> new ABC(t.getT1(), t.getT2().getT1(), t.getT2().getT2()));

    return dependency.a(id)
            .flatMap(a -> dependency.b1(a).zipWith(dependency.c1(a),
                    (b, c) -> new ABC(a, b, c)));
  }

  // ==================================================================================================

  /**
   * Same problem as above but this time define multiple Mono<> variables.
   * !! Use Mono#cache to avoid repeating the call to a(id))
   * a(id), then b1(a) || c1(a) ==> ABC(a,b,c)
   * Relax, you are in Heaven😇: calling b1 and c1 return immediately, without blocking.
   */
  public Mono<ABC> p04_a_then_b1_c1_cache(int id) {
    // codul dinainte de curs:
    //    A a = await dependency.a(id);
    //    B b = await dependency.b1(a);
    //    C c = await dependency.c1(a); // poate in java 24 2-3 ani o sa vedem?

    Mono<A> ma = dependency.a(id).cache(); // PERICULOS SOC: acest cache NU este INTRE requesturi,
    // acest cache traieste cat traieste in heap instanta de mai sus.
    // there are only 2 things hard in programming: 1) cache invalidation  2) naiming things
    Mono<B> mb = ma.flatMap(dependency::b1);
    Mono<C> mc = ma.flatMap(dependency::c1);
    return Mono.zip(ma, mb, mc)
            .map(TupleUtils.function(ABC::new));
    // uite cum repeti apeluri de retea ca pr*stu cu WebFlux. :)
    // - cum poti scrie TESTE UNITARE sa acoperi bugul asta ?

    // - poti totusi hackui codul de mai sus sa NU repete apeluri de retea?
    // da, cu .cache() da n-o face:
    // Idee creatza: .cache() pe orice variabila Mono/Flux ai
    // problema= tii in Heap date si DUPA ce le-ai emis mai jos = DEGEABA pt prea mult timp

    // - ce coding practice poti adopta sa NU patesti asa ceva vreodata?
    // sa nu faci vreodata variabile locale de tip Mono/Flux, ca te arzi.
    // => un chaing enorm de apeluri de functii = "reactive chain"
    // (horror): best practice: o functie ce intoarce Mono incepe pe 1 linie cu return.
  }


  // ==================================================================================================
  //    private static final Many<Integer> objectMany = Sinks.many().multicast()
  //    .onBackpressureBuffer();

  //    Flux<Integer> objectFlux = objectMany.asFlux();
  //    Flux<Integer> objectFlux = objectMany.asFlux();
  //    Flux<Integer> objectFlux = objectMany.asFlux();
  //    Flux<Integer> objectFlux = objectMany.asFlux();
  //    Flux<Integer> objectFlux = objectMany.asFlux();
  //    // din alta parte
  //    objectMany.tryEmitNext(9);


  /**
   * a(id), then b1(a), then c2(a,b) ==> ABC(a,b,c)
   */
  public Mono<ABC> p05_a_then_b1_then_c2(int id) {
    // equivalent blocking⛔️ code:
    //    Mono<A> a = dependency.a(id);
    //    B b = dependency.b1(a).block();
    //    C c = dependency.c2(a, b).block();
    //    return Mono.just(new ABC(a, b, c));

    // TODO Solution #2 (geek): nested flatMap
    //    return dependency.a(id).flatMap(a -> dependency.b1(a).flatMap(b -> dependency.c2(a, b).map(c -> new ABC(a, b, c))));

    // TODO Solution #1: accumulating data structures (chained flatMap)
    //    return dependency.a(id)
    //            .flatMap(a -> dependency.b1(a).map(b -> new AB(a, b)))
    //            .flatMap(ab -> dependency.c2(ab.a, ab.b).map(c -> new ABC(ab.a, ab.b, c)));

    // TODO Solution #1⭐️: accumulating data structures (chained zipWhen)
    return dependency.a(id)
            .zipWhen(a -> dependency.b1(a), (a1, b) -> new AB(a1, b))
            .zipWhen(ab -> dependency.c2(ab.a, ab.b), (ab, c) -> new ABC(ab.a, ab.b, c))
            ;


    // TODO General purpose solution:
    //    in loc de tupleuri, propagi de sus pana jos aceeasi structura de date initial cu nulluri in ea, in care tot pui ce aduci


    // TODO Solution #3 (risky): cached Mono<>
    //      eg Mono<A> ma = dependency.a(id).cache();
  }

  // ==================================================================================================

  /**
   * a(id) then b1(a) ==> AB(a,b)    , but if b(id) returns empty() => AB(a,null)
   * Hint: watch out not to lose the data signals.
   * Challenge is that Flux/Mono cannot carry a "null" data signal.
   * Hint: you might need an operator containing "empty" in its name
   */
  public Mono<AB> p06_a_then_bMaybe(int id) {
    //    return dependency.a(id)
    //            .flatMap(a -> dependency.b1(a).map(b-> new AB(a,b))
    //                    .defaultIfEmpty(new AB(a, null)))
    //            .doOnNext(date -> log.info("Date:  " + date));

    return dependency.a(id)
            .zipWhen(a -> getbMono(a), (a, ob) -> new AB(a, ob.orElse(null)));
  }

  private Mono<Optional<B>> getbMono(A a) {
    return dependency.b1(a).map(b -> Optional.of(b)).defaultIfEmpty(Optional.empty());
  }
  // ==================================================================================================

  /**
   * a(id) || b(id) ==> AB(a,b), but if b(id) returns empty() => AB(a,null)
   * Finish the flow as fast as possible by starting a() in parallel with b()
   * Challenge is that Flux/Mono cannot carry a "null" data signal.
   * Hint: watch out not to lose the data signals.
   */
  public Mono<AB> p07_a_par_bMaybe(int id) {

    return dependency.a(id)
            .zipWith(dependency.b(id).map(Optional::of).defaultIfEmpty(Optional.empty()),
                    (a, bo) -> new AB(a, bo.orElse(null)));

  }

  // ==================================================================================================

  /**
   * a(id) || b(id) ==> AB(a,b), but if b(id) fails => AB(a,null)
   * Hint: watch out not to lose the data signals.
   */
  public Mono<AB> p08_a_try_b(int id) {
    // equivalent blocking⛔️ code:
    //    A a = dependency.a(id).block();
    //    B b = null;
    //    try {
    //      b = dependency.b(id).block();
    //    } catch (Exception e) {
    //    }
    //    return Mono.just(new AB(a, b));

    return dependency.a(id)
            .zipWith(dependency.b(id)
                            .map(Optional::of)
                            .onErrorReturn(Optional.empty()),
                    (a, bo) -> new AB(a, bo.orElse(null)));
  }

  // ==================================================================================================

  /**
   * === UseCase Context Pattern ===
   * The moral of the above example is to avoid saveAudit variables completely.
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

  public Mono<P10UseCaseContext> p10_context(int id) {
    // serial code
    //    return Mono.just(new P10UseCaseContext(id))
    //            .zipWith(dependency.d(id), (context, d) -> context.withD(d))
    //            .zipWith(dependency.a(id), (context, a) -> context.withA(a))
    //            .zipWhen(context -> dependency.b1(context.a), (context, b) -> context.withB(b))
    //            .zipWhen(context -> dependency.c2(context.a, context.b), (context, c) -> context.withC(c));

    return dependency.a(id)
            .flatMap(a -> dependency.b1(a).flatMap(b -> dependency.c2(a, b)
                    .map(c -> new P10UseCaseContext(id).withA(a).withB(b).withC(c))))
            .zipWith(dependency.d(id), P10UseCaseContext::withD);


    //    return Mono.just(new P10UseCaseContext(id))
    //            .flatMap(dependency.a(id)
    //                .zipWhen(a -> dependency.b1(a))
    //                .zipWhen(tab -> dependency.c2(tab.getT1(), tab.getT2()), (tab,c) ->)
    //
    //                    , P10UseCaseContext::withA)
    //            .zipWith(dependency.d(id), P10UseCaseContext::withD)


    // TODO non-blocking and in parallel as many as possible
    //    return Mono.just(context);
  }




}
