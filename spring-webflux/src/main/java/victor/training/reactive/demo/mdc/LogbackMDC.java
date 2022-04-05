package victor.training.reactive.demo.mdc;

import org.slf4j.MDC;
import reactor.core.publisher.Signal;

import java.util.function.Consumer;

public class LogbackMDC {
   public static final String REACTOR_CONTEXT_KEY = "LOGBACK_MDC";

   public static <T> Consumer<Signal<T>> logOnEach(Consumer<T> logStatement) {
      return signal -> {
         String contextValue = signal.getContextView().get(REACTOR_CONTEXT_KEY);
         try (MDC.MDCCloseable mdcCloseable = MDC.putCloseable("MDC_KEY", contextValue)) {
            logStatement.accept(signal.get());
         }
      };
   }

   public static <T> Consumer<Signal<T>> logOnNext(Consumer<T> logStatement) {
      return signal -> {
         if (!signal.isOnNext()) return;
         String contextValue = signal.getContextView().get(REACTOR_CONTEXT_KEY);
         try (MDC.MDCCloseable mdcCloseable = MDC.putCloseable("MDC_KEY", contextValue)) {
            logStatement.accept(signal.get());
         }
      };
   }
}
