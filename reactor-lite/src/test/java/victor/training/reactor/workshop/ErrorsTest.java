package victor.training.reactor.workshop;

import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import victor.training.reactor.workshop.Errors.Dependency;
import victor.training.util.CaptureSystemOutput;
import victor.training.util.CaptureSystemOutput.OutputCapture;
import victor.training.util.NamedThreadFactory;
import victor.training.util.SubscribedProbe;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static reactor.core.publisher.Mono.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodName.class)
@Timeout(1)
class ErrorsTest {
    @Mock
    Dependency dependencyMock;
    @InjectMocks
//    Errors workshop;
    ErrorsSolved workshop;
    @RegisterExtension
    SubscribedProbe subscribed = new SubscribedProbe();

    public static class TestRootCauseException extends RuntimeException {
    }


    private static final ExecutorService secondExecutor = Executors.newFixedThreadPool(1, new NamedThreadFactory("second"));

    @Test
    @CaptureSystemOutput
    void p01_log_KO(OutputCapture outputCapture) {
        when(dependencyMock.call()).thenReturn(Mono.error(new TestRootCauseException()));

        assertThatThrownBy(() -> workshop.p01_log_rethrow().block())
                .isInstanceOf(TestRootCauseException.class);

        assertThat(outputCapture.toString())
                .contains(TestRootCauseException.class.getSimpleName());
    }
    @Test
    @CaptureSystemOutput
    void p01_log_OK(OutputCapture outputCapture) {
        when(dependencyMock.call()).thenReturn(just("abc"));

        workshop.p01_log_rethrow().block();

        assertThat(outputCapture.toString()).isEmpty();
    }

    @Test
    void p02_wrap() {
        when(dependencyMock.call()).thenReturn(Mono.error(new TestRootCauseException()));

        Mono<String> result = workshop.p02_wrap();

        assertThatThrownBy(result::block)
                .isInstanceOf(IllegalStateException.class) // get() wraps the exception within an ExecutionException
                .hasRootCauseInstanceOf(TestRootCauseException.class) // added original exception as the cause
        ;
    }

    @Test
    void p03_defaultValue_KO() {
        when(dependencyMock.call()).thenReturn(Mono.error(new TestRootCauseException()));
        assertThat(workshop.p03_defaultValue().block()).isEqualTo("default");
    }

    @Test
    void p03_defaultValue_OK() {
        when(dependencyMock.call()).thenReturn(just("OK"));
        assertThat(workshop.p03_defaultValue().block()).isEqualTo("OK");
    }


    @Test
    void p04_defaultFuture_OK() {
        when(dependencyMock.call()).thenReturn(just("OK"));
        assertThat(workshop.p04_fallback().block()).isEqualTo("OK");
    }

    @Test
    void p04_defaultFuture_KO() {
        when(dependencyMock.call()).thenReturn(error(new TestRootCauseException()));
        when(dependencyMock.backup()).thenReturn(just("backup"));
        assertThat(workshop.p04_fallback().block()).isEqualTo("backup");
    }

    @Test
    void p05_sendError_OK() {
        when(dependencyMock.call()).thenReturn(just("ok"));
        assertThat(workshop.p05_sendError().block()).isEqualTo("ok");
        verify(dependencyMock, Mockito.never()).sendError(any());
    }
    @Test
    void p05_sendError_KO() {
        TestRootCauseException ex = new TestRootCauseException();
        when(dependencyMock.call()).thenReturn(error(ex));
        when(dependencyMock.sendError(ex)).thenReturn(subscribed.once(empty()));

        assertThatThrownBy(() -> workshop.p05_sendError().block())
                .isInstanceOf(TestRootCauseException.class);
    }


    @Test
    void p06_usingResourceThatNeedsToBeClosed() throws IOException {
        when(dependencyMock.downloadLargeData()).thenReturn(Flux.just("abc","def"));

        workshop.p06_usingResourceThatNeedsToBeClosed().block();

        File file = new File("out.txt");
        assertThat(Files.readString(file.toPath())).isEqualTo("abcdef");
        assertThat(file.delete()).isTrue();
    }




}