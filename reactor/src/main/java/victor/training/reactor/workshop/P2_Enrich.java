package victor.training.reactor.workshop;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;
import reactor.util.function.Tuple3;

import static reactor.function.TupleUtils.function;

@Slf4j
public class P2_Enrich {
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

  public P2_Enrich(Dependency dependency) {
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
//    A a = dependency.a(id).block();
//    B b = dependency.b(id).block();

    return dependency.a(id)
            .zipWith(dependency.b(id), (a, b) -> new AB(a,b));
  }

  // ==================================================================================================

  /**
   * a(id) || b(id) || c(id) ==> ABC(a,b,c)
   */
  public Mono<ABC> p02_a_b_c(int id) {
    // approx equivalent blocking⛔️ code:
//    Mono<A> monoA = dependency.a(id);
//    Mono<B> monoB = dependency.b(id);
//    Mono<C> monoC = dependency.c(id);
//
//    Mono<Tuple3<A, B, C>> monoTuple = Mono.zip(monoA, monoB, monoC);
//
////    Mono<ABC> monoABC = monoTuple.map(t3 -> new ABC(t3.getT1(), t3.getT2(), t3.getT3()));
////    Mono<ABC> monoABC = monoTuple.map(TupleUtils.function((a, b, c) -> new ABC(a, b, c)));
//    Mono<ABC> monoABC = monoTuple.map(function(ABC::new));
//    return monoABC;

    return Mono.zip(dependency.a(id), dependency.b(id), dependency.c(id))
            .map(function(ABC::new));
  }

  // ==================================================================================================

  /**
   * Call a(id) with the given parameter,
   * then b1(a) with the retrieved a;
   * return both a and b.
   * a(id), then b1(a) ==> AB(a,b)
   */
  public Mono<AB> p03_a_then_b1(int id) {
    // approx equivalent blocking⛔️ code:
//     A a = dependency.a(id).block();
//     B b = dependency.b1(a).block();
//     return Mono.just(new AB(a, b));
//    Mono<A> monoA = dependency.a(id);
//
////    Mono<AB> monoAB = monoA.flatMap(a-> dependency.b1(a).map(b-> new AB(a,b)));
////    Mono<AB> monoAB = monoA.zipWhen(a-> dependency.b1(a), (a,b)-> new AB(a,b));
//    Mono<AB> monoAB = monoA.zipWhen(dependency::b1, AB::new);
//
//  return monoAB;

    return dependency.a(id).zipWhen(a -> dependency.b1(a), AB::new);
  }

  // ==================================================================================================

  /**
   * a(id), then b1(a) || c1(a) ==> ABC(a,b,c)
   * Remember, you are in Reactive Heaven😇:
   * calling b1() and c1() launch the network calls and return immediately, without blocking.
   */
  public Mono<ABC> p04_a_then_b1_c1(int id) {
    Mono<A> monoA = dependency.a(id);
    Mono<B> monoB = monoA.flatMap(a -> dependency.b1(a));
    Mono<C> monoC = monoA.flatMap(a -> dependency.c1(a));
    return Mono.zip(monoA, monoB, monoC)
            .map(function((a, b, c) -> new ABC(a, b, c)));
  }

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
   * a(id), then b1(a), then c2(a,b); also d(id) ==> P10UseCaseContext(a,b,c,d)
   */
  @Value // immutable object
  @With // generates: public P10UseCaseContext withA(A newa) { return new P10UseCaseContext(newa, b,c,d); }
  @AllArgsConstructor
  protected static class P10UseCaseContext {
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
    // equivalent blocking⛔️ code:
    P10UseCaseContext context = new P10UseCaseContext(id);
    context = context.withA(dependency.a(context.getId()).block());
    context = context.withB(dependency.b1(context.getA()).block());
    context = context.withC(dependency.c2(context.getA(), context.getB()).block());
    context = context.withD(dependency.d(id).block());
    // propagate context along a chain, enriching it along a chain of .zipWith or .flatMap
    return Mono.just(context);
  }

}
