package victor.training.reactor.workshop;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;
import lombok.extern.slf4j.Slf4j;
import org.springframework.test.annotation.Timed;
import reactor.core.publisher.Mono;

import java.util.concurrent.ExecutionException;

@Slf4j
public class ZComplexFlow {
  @Value static class A {String value;}
  @Value static class B {String value;}
  @Value static class C {String value;}
  @Value static class D {String value;}

  @Value
  @With
  @AllArgsConstructor
  static class MyContext {
    A a;
    B b;
    C c;
    D d;
    A a1;
    public MyContext() {
      this(null, null, null, null, null);}
  }
  public interface Dependency {
    Mono<A> a(int id);
    Mono<B> b(A a);
    Mono<C> c(A a);
//    @Timed()
    Mono<D> d(int id);

    Mono<A> saveA(A a);
    Mono<Void> auditA(A a1, A a0);
  }
  protected final Dependency dependency;

  public ZComplexFlow(Dependency dependency) {
    this.dependency = dependency;
  }

  // ==================================================================================================
  // ⭐️⭐️⭐️ Final Challenge ⭐️⭐️⭐️

  /**
   * a0 = a(id), b = b1(a0), c = c1(a0), d = d(id)
   * --
   * a1=logic(a0,b,c,d)
   * --
   * saveA(a1)
   * auditA(a1,a0); <- !! Don't wait for this to complete (=fire-and-forget),
   *     but make sure any errors in audit are logged in console
   *
   * You are allowed to create any new data structures (immutable, of course)
   * Play: redesign to propagate an Immutable Context (pattern) around
   */
  public Mono<Void> p06_complexFlow(int id) throws ExecutionException, InterruptedException {
    return Mono.zip(dependency.a(id), dependency.d(id), (a, d) -> new MyContext().withA(a).withD(d))
            .flatMap(context -> dependency.b(context.a)
                    .zipWith(dependency.c(context.a), (b, c) -> context.withB(b).withC(c)))
            .map(context -> logic(context))
            .delayUntil(context -> dependency.saveA(context.a1))
            .doOnNext(context -> dependency.auditA(context.getA1(), context.a).doOnError(e -> log.error("Valeu " + e)).subscribe())
            .then();


    // equivalent blocking⛔️ code:
    // 1. data fetching

    // 2. logic
//    A a1 = logic(a0, b, c, d);
//
//    // 3. save & side effects
//    dependency.saveA(a1).block();
//    dependency.auditA(a1, a0); // <- don't wait for this to complete
//    return Mono.just(null);
  }

  public MyContext logic(MyContext context) {

    // Imagine Dragons (logic)
    A a1 = new A(context.a.value + context.b.value.toUpperCase() + context.c.value + context.d.value);
    return context.withA1(a1);
  }
}
