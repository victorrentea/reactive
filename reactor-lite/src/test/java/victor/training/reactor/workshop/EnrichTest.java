package victor.training.reactor.workshop;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import victor.training.reactor.workshop.Enrich.*;
import victor.training.util.SubscribedProbe;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static reactor.core.publisher.Mono.*;
import static victor.training.util.RunAsNonBlocking.runsNonBlocking;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodName.class)
public class EnrichTest {
    @Mock
    Dependency dependency;
    @InjectMocks
    EnrichSolved workshop;

    private static final A a = new A();
    private static final B b = new B();
    private static final C c = new C();
    private static final D d = new D();

    @RegisterExtension
    SubscribedProbe subscribed = new SubscribedProbe();

    @Test
    void p01_a_par_b() {
        when(dependency.a(1)).thenReturn(subscribed.once(just(a)));
        when(dependency.b(1)).thenReturn(subscribed.once(just(b)));

        assertThat(workshop.p01_a_par_b(1).block()).isEqualTo(new AB(a, b));
    }

    @Test
    void p02_a_b_c() {
        when(dependency.a(1)).thenReturn(subscribed.once(just(a)));
        when(dependency.b(1)).thenReturn(subscribed.once(just(b)));
        when(dependency.c(1)).thenReturn(subscribed.once(just(c)));

        assertThat(workshop.p02_a_b_c(1).block()).isEqualTo(new ABC(a, b,c));
    }

    @Test
    void p03_a_then_b1() {
        when(dependency.a(1)).thenReturn(subscribed.once(just(a)));
        when(dependency.b1(a)).thenReturn(subscribed.once(just(b)));

        assertThat(workshop.p03_a_then_b1(1).block()).isEqualTo(new AB(a, b));
    }
    @Test
    void p04_a_then_b1_c1() {
        when(dependency.a(1)).thenReturn(subscribed.once(just(a)));
        when(dependency.b1(a)).thenReturn(subscribed.once(just(b)));
        when(dependency.c1(a)).thenReturn(subscribed.once(just(c)));

        assertThat(workshop.p04_a_then_b1_c1(1).block()).isEqualTo(new ABC(a, b,c));
    }
    @Test
    void p04_a_then_b1_c1_cache() {
        when(dependency.a(1)).thenReturn(subscribed.once(just(a)));
        when(dependency.b1(a)).thenReturn(subscribed.once(just(b)));
        when(dependency.c1(a)).thenReturn(subscribed.once(just(c)));

        assertThat(workshop.p04_a_then_b1_c1_cache(1).block()).isEqualTo(new ABC(a, b,c));
    }

    @Test
    void p05_a_then_b1_then_c2() {
        when(dependency.a(1)).thenReturn(subscribed.once(just(a)));
        when(dependency.b1(a)).thenReturn(subscribed.once(just(b)));
        when(dependency.c2(a, b)).thenReturn(subscribed.once(just(c)));

        Mono<ABC> mono = runsNonBlocking(() -> workshop.p05_a_then_b1_then_c2(1));

        assertThat(mono.block()).isEqualTo(new ABC(a, b, c));
    }
    @Test
    void p06_a_then_bMaybe() {
        when(dependency.a(1)).thenReturn(subscribed.once(just(a)));
        when(dependency.b1(a)).thenReturn(subscribed.once(just(b)));

        Mono<AB> mono = runsNonBlocking(() -> workshop.p06_a_then_bMaybe(1));

        assertThat(mono.block()).isEqualTo(new AB(a, b));
    }
    @Test
    void p06_a_then_bMaybe_noB() {
        when(dependency.a(1)).thenReturn(subscribed.once(just(a)));
        when(dependency.b1(a)).thenReturn(subscribed.once(empty()));

        Mono<AB> mono = runsNonBlocking(() -> workshop.p06_a_then_bMaybe(1));

        assertThat(mono.block()).isEqualTo(new AB(a, null));
    }

    @Test
    @Timeout(1)
    void p07_a_par_bMaybe_inparallel() {
        when(dependency.a(1)).thenReturn(subscribed.once(just(a).delayElement(Duration.ofMillis(700))));
        when(dependency.b(1)).thenReturn(subscribed.once(just(b).delayElement(Duration.ofMillis(700))));

        Mono<AB> mono = runsNonBlocking(() -> workshop.p07_a_par_bMaybe(1));

        assertThat(mono.block()).isEqualTo(new AB(a, b));
    }
    @Test
    void p07_a_par_bMaybe_noB() {
        when(dependency.a(1)).thenReturn(subscribed.once(just(a)));
        when(dependency.b(1)).thenReturn(subscribed.once(empty()));

        Mono<AB> mono = runsNonBlocking(() -> workshop.p07_a_par_bMaybe(1));

        assertThat(mono.block()).isEqualTo(new AB(a, null));
    }
    @Test
    void p08_a_try_b() {
        when(dependency.a(1)).thenReturn(subscribed.once(just(a)));
        when(dependency.b(1)).thenReturn(subscribed.once(just(b)));

        Mono<AB> mono = runsNonBlocking(() -> workshop.p08_a_try_b(1));

        assertThat(mono.block()).isEqualTo(new AB(a, b));
    }
    @Test
    void p08_a_try_b_KO() {
        when(dependency.a(1)).thenReturn(subscribed.once(just(a)));
        when(dependency.b(1)).thenReturn(subscribed.once(error(new IllegalStateException())));

        Mono<AB> mono = runsNonBlocking(() -> workshop.p08_a_try_b(1));

        assertThat(mono.block()).isEqualTo(new AB(a, null));
    }

    @Test
    void p10_context() {
        when(dependency.a(1)).thenReturn(subscribed.once(just(a)));
        when(dependency.b1(a)).thenReturn(subscribed.once(just(b)));
        when(dependency.c2(a, b)).thenReturn(subscribed.once(just(c)));
        when(dependency.d(1)).thenReturn(subscribed.once(just(d)));

        Mono<P10UseCaseContext> mono = runsNonBlocking(() -> workshop.p10_context(1));

        assertThat(mono.block()).isEqualTo(new P10UseCaseContext(1, a,b,c,d));
    }


    //    @Nested
//    class P06_ComplexFlow {
//        @Captor
//        ArgumentCaptor<A> captorA;
//        @BeforeEach
//        final void before() {
//            when(dependency.a(1)).thenReturn(completedFuture(a));
//            lenient().when(dependency.b1(a)).thenReturn(completedFuture(b));
//            lenient().when(dependency.c1(a)).thenReturn(completedFuture(c));
//        }
//        @Test
//        void happy() throws ExecutionException, InterruptedException {
//            when(dependency.saveA(any())).thenAnswer(x -> completedFuture(x.getArgument(0)));
//            when(dependency.auditA(any(), eq(a))).thenReturn(completedFuture(null));
//
//            workshop.p06_complexFlow(1).get();
//
//            verify(dependency).a(1); // called once
//            verify(dependency).b1(a); // called once
//            verify(dependency).c1(a); // called once
//            verify(dependency).saveA(captorA.capture()); // called once
//            A a1 = captorA.getValue();
//            assertThat(a1.a).isEqualTo("aBc");
//            verify(dependency).auditA(a1, a); // called once
//        }
//
//        @Test
//        @Timeout(value = 400, unit = MILLISECONDS)
//        void doesNotWaitForAuditToComplete() throws ExecutionException, InterruptedException {
//            when(dependency.saveA(any())).thenAnswer(x -> completedFuture(x.getArgument(0)));
//            when(dependency.auditA(any(), eq(a))).thenReturn(supplyAsync(() -> null, delayedExecutor(500, MILLISECONDS)));
//
//            workshop.p06_complexFlow(1).get();
//
//            verify(dependency).saveA(any());
//        }
//        @Test
//        @CaptureSystemOutput
//        void doesNotFail_ifAuditFails_butErrorIsLogged(OutputCapture outputCapture) throws ExecutionException, InterruptedException {
//            when(dependency.saveA(any())).thenAnswer(x -> completedFuture(x.getArgument(0)));
//            when(dependency.auditA(any(), eq(a))).thenReturn(failedFuture(new NullPointerException("from test")));
//
//            workshop.p06_complexFlow(1).get();
//
//            verify(dependency).saveA(any());
//            assertThat(outputCapture.toString()).contains("from test");
//        }
//        @Test
//        void errorInB_failsTheWholeFlow() throws ExecutionException, InterruptedException {
//            when(dependency.b1(a)).thenReturn(failedFuture(new NullPointerException("from test")));
//
//            assertThatThrownBy(() -> workshop.p06_complexFlow(1).get());
//
//            verify(dependency,never()).saveA(any());
//            verify(dependency,never()).auditA(any(), any());
//        }
//
//        @Test
//        void errorInSave_doesNotAudit() throws ExecutionException, InterruptedException {
//            when(dependency.saveA(any())).thenReturn(failedFuture(new NullPointerException("from test")));
//
//            assertThatThrownBy(() -> workshop.p06_complexFlow(1).get());
//
//            verify(dependency,never()).auditA(any(), any());
//        }
//
//        @Test
//        @Timeout(value = 900, unit = MILLISECONDS)
//        @Disabled("EXTRA HARD")
//        void calls_b_c_inParallel() throws ExecutionException, InterruptedException {
//            // when(dependency.b1(a)).thenReturn(supplyAsync(() -> b, delayedExecutor(500, MILLISECONDS))); // WRONG
//            // Note: thenAnswer(->) calls the -> only when invoked from tested code
//            // ==> start ticking the 500 millis only AT PROD CALL, not earlier
//
//            when(dependency.b1(a)).thenAnswer(x->supplyAsync(() -> b, delayedExecutor(500, MILLISECONDS)));
//            when(dependency.c1(a)).thenAnswer(x->supplyAsync(() -> c, delayedExecutor(500, MILLISECONDS)));
//            when(dependency.saveA(any())).thenAnswer(x -> completedFuture(x.getArgument(0)));
//            when(dependency.auditA(any(), eq(a))).thenReturn(completedFuture(null));
//
//            workshop.p06_complexFlow(1).get();
//        }
//
//    }
    // parallel fetch ?
}
