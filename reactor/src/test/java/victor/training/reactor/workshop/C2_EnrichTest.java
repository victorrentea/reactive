package victor.training.reactor.workshop;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import victor.training.reactor.workshop.C2_Enrich.*;
import victor.training.util.SubscribedProbe;

import java.time.Duration;

import static java.time.Duration.ofMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static reactor.core.publisher.Mono.*;
import static victor.training.util.RunAsNonBlocking.nonBlocking;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodName.class)
public class C2_EnrichTest {
    @Mock
    Dependency dependency;
    @InjectMocks
    protected C2_Enrich workshop;

    private static final A a = new A();
    private static final B b = new B();
    private static final C c = new C();
    private static final D d = new D();

    @BeforeEach
    final void before() {
        System.out.println("workshop="+workshop);
//        System.out.println("workshop2="+workshop2);
    }
    @RegisterExtension
    SubscribedProbe subscribed = new SubscribedProbe();

    @Test
    void p01_a_par_b() {
        when(dependency.a(1)).thenReturn(subscribed.once(just(a)));
        when(dependency.b(1)).thenReturn(subscribed.once(just(b)));

        assertThat(workshop.p01_a_par_b(1).block()).isEqualTo(new AB(a, b));
    }

//    @Test
//    void p01_a_par_b_on_error() {
//        when(dependency.a(1)).thenReturn(subscribed.once(error(new RuntimeException("DUMMY"))));
//        when(dependency.b(1)).thenReturn(subscribed.once(just(b)));
//
//        workshop.p01_a_par_b(1).block();
////        assertThatThrownBy(()->workshop.p01_a_par_b(1).block());
//    }

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
    void p04_a_then_b1_c1_cache_naiveTest() {
        // given
        Mono<A> justA = Mono.just(a);
        // TODO how can I count how many times was subscribed the publisher above?
        when(dependency.a(1)).thenReturn(justA);
        when(dependency.b1(a)).thenReturn(just(b));
        when(dependency.c1(a)).thenReturn(just(c));

        // when
        ABC block = workshop.p04_a_then_b1_c1_cache(1).block();

        // then
        assertThat(block).isEqualTo(new ABC(a, b, c));
    }

    @Test
    void p04_a_then_b1_c1_cache() {
        // subscribed.once used here to prevent multiple subscriptions == multiple network calls in prod
        when(dependency.a(1)).thenReturn(subscribed.once(Mono.just(a)));
        when(dependency.b1(a)).thenReturn(subscribed.once(Mono.just(b)));
        when(dependency.c1(a)).thenReturn(subscribed.once(Mono.just(c)));

        Mono<ABC> monoAbc = workshop.p04_a_then_b1_c1_cache(1);

        assertThat(monoAbc.block()).isEqualTo(new ABC(a, b,c));
    } // after @Test is exited the SubscribedProbe JUnit Extension will check that each Mono was subscribed only once

    @Test
    void p05_a_then_b1_then_c2() {
        when(dependency.a(1)).thenReturn(subscribed.once(just(a)));
        when(dependency.b1(a)).thenReturn(subscribed.once(just(b)));
        when(dependency.c2(a, b)).thenReturn(subscribed.once(just(c)));

        ABC abc = nonBlocking(() -> workshop.p05_a_then_b1_then_c2(1));

        assertThat(abc).isEqualTo(new ABC(a, b, c));
    }
    @Test
    void p06_a_then_bMaybe() {
        when(dependency.a(1)).thenReturn(subscribed.once(just(a)));
        when(dependency.b1(a)).thenReturn(subscribed.once(just(b)));

        AB ab = nonBlocking(() -> workshop.p06_a_then_bMaybe(1));

        assertThat(ab).isEqualTo(new AB(a, b));
    }
    @Test
    void p06_a_then_bMaybe_noB() {
        when(dependency.a(1)).thenReturn(subscribed.once(just(a)));
        when(dependency.b1(a)).thenReturn(subscribed.once(empty()));

        AB ab = nonBlocking(() -> workshop.p06_a_then_bMaybe(1));

        assertThat(ab).isEqualTo(new AB(a, null));
    }

    @Test
    @Timeout(1)
    void p07_a_par_bMaybe_inparallel() {
        when(dependency.a(1)).thenReturn(subscribed.once(just(a).delayElement(ofMillis(700))));
        when(dependency.b(1)).thenReturn(subscribed.once(just(b).delayElement(ofMillis(700))));

        AB ab = nonBlocking(() -> workshop.p07_a_par_bMaybe(1));

        assertThat(ab).isEqualTo(new AB(a, b));
    }

    @Test
    void p07_a_par_bMaybe_noB() {
        when(dependency.a(1)).thenReturn(subscribed.once(just(a)));
        when(dependency.b(1)).thenReturn(subscribed.once(empty()));

        AB ab = nonBlocking(() -> workshop.p07_a_par_bMaybe(1));

        assertThat(ab).isEqualTo(new AB(a, null));
    }
    @Test
    void p08_a_try_b() {
        when(dependency.a(1)).thenReturn(subscribed.once(just(a)));
        when(dependency.b(1)).thenReturn(subscribed.once(just(b)));

        AB ab = nonBlocking(() -> workshop.p08_a_try_b(1));

        assertThat(ab).isEqualTo(new AB(a, b));
    }
    @Test
    void p08_a_try_b_KO() {
        when(dependency.a(1)).thenReturn(subscribed.once(just(a)));
        when(dependency.b(1)).thenReturn(subscribed.once(error(new IllegalStateException())));

        AB ab = nonBlocking(() -> workshop.p08_a_try_b(1));

        assertThat(ab).isEqualTo(new AB(a, null));
    }

    @Test
    @Timeout(value = 700, unit = MILLISECONDS)
    void p10_contextPattern() {
        when(dependency.a(1)).thenReturn(subscribed.once(just(a).delayElement(ofMillis(100))));
        when(dependency.b1(a)).thenReturn(subscribed.once(just(b).delayElement(ofMillis(200))));
        when(dependency.c2(a, b)).thenReturn(subscribed.once(just(c).delayElement(ofMillis(300))));
        when(dependency.d(1)).thenReturn(subscribed.once(just(d).delayElement(ofMillis(400))));

        P10Context context = nonBlocking(() -> workshop.p10_contextPattern(1));

        assertThat(context).isEqualTo(new P10Context(1, a,b,c,d));
    }
}
