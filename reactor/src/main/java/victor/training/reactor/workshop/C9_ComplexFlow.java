package victor.training.reactor.workshop;

import lombok.AllArgsConstructor;
import lombok.With;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.concurrent.ExecutionException;

public class C9_ComplexFlow {
  protected static final Logger log = LoggerFactory.getLogger(C9_ComplexFlow.class);

  protected record A(String value) { }
  protected record B(String value) { }
  protected record C(String value) { }
  protected record D(String value) { }

  @With
  protected record MyContext(A a, B b, C c, D d, A a1) {
    public MyContext() {
      this(null, null, null, null, null);
    }
  }
  protected interface Dependency {
    Mono<A> a(int id);
    Mono<B> b(A a);
    Mono<C> c(A a);
    Mono<D> d(int id);

    Mono<A> saveA(A a);
    Mono<Void> auditA(A a1, A a0);
  }
  protected final Dependency dependency;

  public C9_ComplexFlow(Dependency dependency) {
    this.dependency = dependency;
  }

  /**
   * READ: a0 = a(id), b = b1(a0), c = c1(a0), d = d(id)
   * --
   * PROCESS: a1=logic(a0,b,c,d)
   * --
   * WRITE:
   * saveA(a1)
   * auditA(a1,a0); <- !! Don't wait for this to complete (=fire-and-forget),
   *     but make sure any errors in audit are logged in console
   * RETURN saved a.id
   *
   * You are allowed to create any new data structures (immutable, of course)
   * Play: redesign to propagate an Immutable Context (pattern) around
   */
  public Mono<Void> p06_complexFlow(int id) throws ExecutionException, InterruptedException {
    // equivalent, broken blocking⛔️ code:
    // 1. READ
    A a0 = dependency.a(id).block();
    B b = dependency.b(a0).block();
    C c = dependency.c(a0).block();
    D d = dependency.d(id).block();

    // 2. PROCESS
    A a1 = logic(a0, b, c, d);

    // 3. save & side effects
    dependency.saveA(a1).block();
    dependency.auditA(a1, a0); // <- don't wait for this to complete
    return Mono.empty();
  }

  public A logic(A a, B b, C c, D d) {
    // Imagine Dragons (logic)
    A a1 = new A(a.value + b.value.toUpperCase() + c.value + d.value);
    return a1;
  }
}
