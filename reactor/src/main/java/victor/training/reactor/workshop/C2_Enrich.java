package victor.training.reactor.workshop;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Optional;

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
    var mabc = Mono.zip(ma, mb, mc);//Function<P1,P2,P3,R>
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
    return dependency.a(id) // ❤️❤️❤️ = expression function
        .flatMap(a -> Mono.zip(
            dependency.b1(a),
            dependency.c1(a),
            (b, c) -> new ABC(a, b, c)));
  }
  // Hint mono.flatMap(->mono.zipWith(mono, ->))

  // ==================================================================================================

  /**
   * Solve the same problem as above, by using multiple Mono<> variables.
   * Hint: Use Mono#cache to avoid repeating the call to a(id)
   */
  public Mono<ABC> p04_a_then_b1_c1_cache(int id) {
    // PROBLEM: A api call called 3x for subscribing 3 times to it: mb,mc,zip
//    Mono<A> ma = dependency.a(id);
//    Mono<B> mb = ma.flatMap(dependency::b1);
//    Mono<C> mc = ma.flatMap(dependency::c1);
//    return Mono.zip(ma, mb, mc).map(function(ABC::new));

    // ☢️ impossible to spot this issue in Code Review ⚠️
    // 👍 Takeaway: never define variables of type Mono/Flux ⭐️⭐️⭐️
    Mono<A> ma = dependency.a(id).cache(); // ⚠️safe against concurrent subscriptions
    // someone will forget .cache one day => resubscribing
    return Mono.zip(ma,
            ma.flatMap(dependency::b1),
            ma.flatMap(dependency::c1))
        .map(function(ABC::new));
  }

  // expression functions: functions whose body is a single expression
  // = extreme functional programming style

  // Reactive Programming = Reactive Functional Programming

//  @Cacheable
//  Data fetchExpensiveData() {
//     10s
//  }

  // ==================================================================================================

  /**
   * a(id), then b1(a), then c2(a,b) ==> ABC(a,b,c)
   */
  public Mono<ABC> p05_a_then_b1_then_c2(int id) {
    // TODO Solution #1: "traditional" .cache() -> avoid!❌
//    var ma = dependency.a(id).cache();
//    var mb = ma.flatMap(a->dependency.b1(a)).cache();
////    var mc = Mono.zip(ma, mb, (a,b)-> dependency.c2(a, b))
////        .flatMap(Function.identity()) // m->m; put the contents of the inner box into the outer box
////                                      // [[1]] -> [1]
////        /*.cache()*/; // not useful here;dangerous
//    var mc = Mono.zip(ma, mb)
//        .flatMap(tuple -> dependency.c2(tuple.getT1(), tuple.getT2()))
//        .cache(); // for habbit
//    return Mono.zip(ma,mb,mc).map(function(ABC::new));

    // =============================
    // EXTREME FP = CORRECT REACTIVE WAY @aditya ❤️❤️❤️❤️❤️❤️❤️
    // TODO Solution #2 nested flatMap
    var result = dependency.a(id)
        .flatMap(a -> dependency.b1(a)
            .flatMap(b -> dependency.c2(a,b)
                .map(c -> new ABC(a,b,c))));

    // =============================
    // TODO Solution #3: accumulating data structures (linear flatMap/zipWhen)
    // aka Context pattern
//    var result = dependency.a(id)
//        .zipWhen(a -> dependency.b1(a), AB::new)
//        .zipWhen(ab -> dependency.c2(ab.a, ab.b),
//            (tab,c)->new ABC(tab.a, tab.b, c));

    return result;
  }

  // ==================================================================================================

  /**
   * a(id) then b1(a) ==> AB(a,b), but if b1(a) returns empty() => return AB(a,null)
   * a=user
   * b=pshycho profile
   */
  public Mono<AB> p06_a_then_bMaybe(int id) {
    return dependency.a(id)
        .flatMap(a -> dependency.b1(a)
            .map(b -> new AB(a, b))
            .defaultIfEmpty(new AB(a, null)));
        // OptionalB.map(b->new AB(a,b)).orElse(new AB(a,null))

  }

  // a+b => AB(a,b)
  // a+null=>AB(a,null)
  // null+b=>AB(null,b)
  public Mono<AB> method(int aId, int bId) {
    return Mono.zip(
        // I NEED a data signal anyway in zip!!!, not an empty publisher
        dependency.a(aId)
            .map(Optional::of)
            .defaultIfEmpty(Optional.empty()),
        dependency.b(bId)
            .map(Optional::of)
            .defaultIfEmpty(Optional.empty()),
//            .switchIfEmpty(Mono.just(Optional.empty())), overkill
        (oa, ob) -> new AB(oa.orElse(null), ob.orElse(null))
    );
  }
  // 1=boring; 3=business as usual; 5=bind-blown

//           redisCache.findById(id).switchIfEmpty(repo.findById(id));

  // new AB(a(id)? , b(id)?)

  // ==================================================================================================

  /**
   * a(id) || b(id) ==> AB(a,b), but if b(id) returns empty() => return AB(a,null)
   * ⚠️ Watch out not to lose the data signals.
   * Challenge: Reactor's Flux/Mono can never emit a "null" data signal.
   * Finish the flow as fast as possible by starting a() in parallel with b()
   */
  public Mono<AB> p07_a_par_bMaybe(int id) {
    return Mono.zip(dependency.a(id),
        dependency.b(id).map(Optional::of).defaultIfEmpty(Optional.empty()),
        (a, b) -> new AB(a, b.orElse(null)));
  }

  // ==================================================================================================

  /**
   * a(id) || b(id) ==> AB(a,b)
   * but if b(id) returns ERROR => return AB(a,null)
   */
  public Mono<AB> p08_a_try_b(int id) {
//    A a = dependency.a(id).block();
//    B b = null;
//    try {
//      b = dependency.b(id).block();
//    } catch (Exception e) {
//    }
//    return Mono.just(new AB(a, b));
    return Mono.zip(
        dependency.a(id),
        dependency.b(id)
            .map(Optional::of)
            .onErrorReturn(Optional.empty()), // lookup in some flaky system
        (a, ob) -> new AB(a, ob.orElse(null)));
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
//    public P10Context withA(A a) { // @With generates this automatically
//      return this.a == a ? this : new P10Context(this.id, a, this.b, this.c, this.d);
//    }
    public P10Context(int id) { // initial UC parameters
      this(id, null, null, null, null);
    }

    public void logMe() {
      log.info("{}", this);
    }
  }

  public Mono<P10Context> p10_contextPattern(int id) {
//    return Mono.just(new P10Context(id))
//        .zipWhen(ctx-> dependency.a(ctx.getId()), P10Context::withA)
//            // 👍§ NEVER do P10Context::withA again on this chain
//        .zipWhen(ctx -> dependency.b1(ctx.a), P10Context::withB)
//        .zipWhen(ctx -> dependency.c2(ctx.a, ctx.b), P10Context::withC)
//
//        .zipWith(dependency.d(id), P10Context::withD);

    return Mono.just(new P10Context(id))
        .zipWhen(ctx-> dependency.a(ctx.getId()), P10Context::withA)
        .doOnNext(P10Context::logMe)
            // 👍§ NEVER do P10Context::withA again on this chain
        .zipWhen(ctx -> dependency.b1(ctx.a), P10Context::withB)
        .zipWhen(ctx -> dependency.c2(ctx.a, ctx.b), P10Context::withC)

        .zipWith(dependency.d(id), P10Context::withD); // - 400ms
//        .zipWhen(ctx->dependency.d(id), P10Context::withD);
  }

}
