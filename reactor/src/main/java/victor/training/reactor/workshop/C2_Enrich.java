package victor.training.reactor.workshop;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;

import static reactor.function.TupleUtils.function;

@Slf4j
public class C2_Enrich {
  protected static class A {
  }

  protected static class B {
  }

  protected static class C {
    boolean reject;
  }

  protected static class D {
  }

  @Value
  @With
  protected static class AB {
    public A a;
    public B b;
  }

  @Value
  protected static class ABC {
    public A a;
    public B b;
    public C c;
  }

  protected interface Dependency {
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

  public C2_Enrich(Dependency dependency) {
    this.dependency = dependency;
  }

  // ⚠️ ATTENTION ⚠️ ENTERING REACTIVE HEAVEN ⚠️
  //  😇 😇 😇 😇 😇 😇 😇 😇 😇 😇 😇 😇 😇 😇 😇 😇 😇 😇 😇 😇 😇
  //
  // * ALL THE NETWORK CALLS HAVE BEEN CAREFULLY WRAPPED IN NON-BLOCKING FUNCTIONS
  //   eg. relying on WebClient for REST calls
  //   + reactive drivers for SQL (R2DBC), Mongo, Kafka, Redis, Cassandra, ..
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
    // approx equivalent blocking⛔️ code:

    // ⭐️ do not .block() in /src/main/** in all but boundedElastic Scheduler
    // can hijack the promises of massive parallelism that reactive gives

    // 👍 if you ever call network, that method must return a Publisher (mono/flux)
    var ma = dependency.a(id); // 1s
    var mb = dependency.b(id); // 2s

//    Mono<Object> objectMono = Mono.firstWithValue(ma, mb);

    return ma.zipWith(mb, (a, b) -> new AB(a, b)); // if ma is "more important"
//    return Mono.zip(ma, mb, AB::new); // === identical, or ≥3
    // a() and b() network calls happen in PARALLEL,
    // that is: returned mono emits after 2s after subscription

    // Java CompletableFuture equivalent:
    // CompletableFuture<A> futureA = supplyAsync(() -> blockingA(id));
    // CompletableFuture<B> futureB = supplyAsync(() -> blockingB(id));
    // return futureA.thenCombine(futureB, (a, b) -> new AB(a, b));
  }

  // ☢️☢️☢️ Zip RACE 😱😱😱
  // Mono<Void> sideEffect1; // no data, just completion
  // Mono<UUID> createEffect2; // emits the generated id
  // sideEffect1.zipWith(createEffect2).then();

  // Case1) #2 completes first, #1 completes second => ✅ both finish
  // Case2) #1 completes first, #2 completes second => ❌ #2 is interrupted (cancelled)

  // FIX: add .thenReturn("dummy") before zipWith above!

  // ==================================================================================================

  /**
   * a(id) || b(id) || c(id) ==> ABC(a,b,c)
   */
  public Mono<ABC> p02_a_b_c(int id) {
    // approx equivalent blocking⛔️ code:
    var ma = dependency.a(id);
    var mb = dependency.b(id);
    var mc = dependency.c(id);
    var mabc = Mono.zip(ma,mb,mc);//Function<P1,P2,P3,R>
//    return mabc.map(t -> new ABC(t.getT1(), t.getT2(), t.getT3()));

    // using reactor-utils
//    return mabc.map(TupleUtils.function((a,b,c)->new ABC(a,b,c)));
//    return mabc.map(TupleUtils.function(ABC::new));
    return mabc.map(function(ABC::new));
  }

  // ==================================================================================================

  /**
   * Call a(id) with the given parameter,
   * then b1(a) with the retrieved a;
   * return both a and b.
   * a(id), then b1(a) ==> AB(a,b)
   */
  public Mono<AB> p03_a_then_b1(int id) {
//     A a = dependency.a(id).block(); // find user by id
//     B b = dependency.b1(a).block(); // by the user.departmentId, fetch department by id
//     return Mono.just(new AB(a, b)); // return user.name + dept name

    return dependency.a(id)
//        .flatMap(a->dependency.b1(a).map(b->new AB(a,b))) // same, but uglier
        .zipWhen(dependency::b1, AB::new);//✅
  }
  // use .map when your -> does not do IO
  // use .flatMap when your -> hits network
  // ==================================================================================================

  /**
   * a(id), then b1(a) || c1(a) ==> ABC(a,b,c)
   * Remember, you are in Reactive Heaven😇:
   * calling b1() and c1() launch the network calls and return immediately, without blocking.
   */
  public Mono<ABC> p04_a_then_b1_c1(int id) {
    var result =  dependency.a(id)
        .zipWhen(a-> Mono.zip(dependency.b1(a), dependency.c1(a))
            .map(tuple ->new ABC(a,tuple.getT1(), tuple.getT2())))
        .map(t->t.getT2());
    return result;
  }
    // Hint mono.flatMap(->mono.zipWith(mono, ->))

  // ==================================================================================================

  /**
   * Solve the same problem as above, by using multiple Mono<> variables.
   * Hint: Use Mono#cache to avoid repeating the call to a(id)
   */
  public Mono<ABC> p04_a_then_b1_c1_cache(int id) {
    Mono<A> ma = dependency.a(id);
    // Mono<B> mb =
    // etc...
    return null;
  }


  // ==================================================================================================

  /**
   * a(id), then b1(a), then c2(a,b) ==> ABC(a,b,c)
   */
  public Mono<ABC> p05_a_then_b1_then_c2(int id) {
    // equivalent blocking⛔️ code:
    A a = dependency.a(id).block();
    B b = dependency.b1(a).block();
    C c = dependency.c2(a, b).block();
    return Mono.just(new ABC(a, b, c));

    // TODO Solution #1: accumulating data structures (chained flatMap)

    // TODO Solution #2 (geek): nested flatMap

    // TODO Solution #3 (risky): cached Mono<>
    //      eg Mono<A> ma = dependency.a(id).cache();
  }

  // ==================================================================================================

  /**
   * a(id) then b1(a) ==> AB(a,b), but if b1(a) returns empty() => return AB(a,null)
   * ⚠️ Watch out not to lose the data signals.
   * Challenge: Reactor's Flux/Mono can never emit a "null" data signal.
   * Hint: you might need an operator containing "empty" in its name
   */
  public Mono<AB> p06_a_then_bMaybe(int id) {
    // equivalent blocking⛔️ code:
    A a = dependency.a(id).block();
    B b = dependency.b1(a).block();
    return Mono.just(new AB(a, b));
  }

  // ==================================================================================================

  /**
   * a(id) || b(id) ==> AB(a,b), but if b(id) returns empty() => return AB(a,null)
   * ⚠️ Watch out not to lose the data signals.
   * Challenge: Reactor's Flux/Mono can never emit a "null" data signal.
   * Finish the flow as fast as possible by starting a() in parallel with b()
   */
  public Mono<AB> p07_a_par_bMaybe(int id) {
    // equivalent blocking⛔️ code:
    A a = dependency.a(id).block(); // in parallel
    B b = dependency.b(id).block(); // in parallel
    return Mono.just(new AB(a, b));
  }

  // ==================================================================================================

  /**
   * a(id) || b(id) ==> AB(a,b), but if b(id) returns ERROR => return AB(a,null)
   * ⚠️ Watch out not to lose the data signals.
   */
  public Mono<AB> p08_a_try_b(int id) {
    // equivalent blocking⛔️ code:
    A a = dependency.a(id).block();
    B b = null;
    try {
      b = dependency.b(id).block();
    } catch (Exception e) {
    }
    return Mono.just(new AB(a, b));
  }

  // ==================================================================================================

  /**
   * === UseCase Context Pattern ===
   * The moral of the above example is to avoid method variables completely.
   * a(id), then b1(a), then c2(a,b); also d(id) ==> P10Context(a,b,c,d)
   */
  @Value // immutable object
  @With // generates: public P10Context withA(A newa) { return new P10Context(newa, b,c,d); }
  @AllArgsConstructor
  protected static class P10Context {
    int id;
    A a;
    B b;
    C c;
    D d;

    public P10Context(int id) { // initial UC parameters
      this(id, null, null, null, null);
    }
  }

  public Mono<P10Context> p10_contextPattern(int id) {
    // equivalent blocking⛔️ code:
    P10Context context = new P10Context(id);
    context = context.withA(dependency.a(context.getId()).block());
    context = context.withB(dependency.b1(context.getA()).block());
    context = context.withC(dependency.c2(context.getA(), context.getB()).block());
    context = context.withD(dependency.d(id).block());
    // propagate context along a chain, enriching it along a chain of .zipWith or .flatMap
    return Mono.just(context);
  }

}
