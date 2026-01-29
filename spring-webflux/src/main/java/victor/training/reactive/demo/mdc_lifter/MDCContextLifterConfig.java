package victor.training.reactive.demo.mdc_lifter;

import org.reactivestreams.Subscription;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Operators;
import reactor.util.context.Context;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Configuration
public class MDCContextLifterConfig {
   private static final String MDC_CONTEXT_REACTOR_HOOK_KEY = "MDC_ON_REACTOR_CONTEXT";

   @PostConstruct // TODO try comment out
   public void setUpOperatorHook() {
      Hooks.onEachOperator(MDC_CONTEXT_REACTOR_HOOK_KEY, Operators.lift(
          (scannable, subscriber) -> new MdcContextLifter<>(subscriber)));
   }

   @PreDestroy
   public void cleanupHook() {
      Hooks.resetOnEachOperator();
   }

   private static class MdcContextLifter<T> implements CoreSubscriber<T> {
      private final CoreSubscriber<T> coreSubscriber;

      private MdcContextLifter(CoreSubscriber<T> coreSubscriber) {
         this.coreSubscriber = coreSubscriber;
      }

      @Override
      public void onSubscribe(Subscription subscription) {
         coreSubscriber.onSubscribe(subscription);
      }

      @Override
      public void onNext(T t) {
         copyToMdc(coreSubscriber.currentContext());
         coreSubscriber.onNext(t);
      }

      private void copyToMdc(Context context) {
         if (!context.isEmpty()) {
            Map<String, String> map = context.stream().collect(toMap(
                e -> e.getKey().toString(),
                e -> e.getValue().toString()));
            // System.out.println("Restoring Reactor>MDC context on thread " + Thread.currentThread().getName() + " keys:" + map.keySet().toString());
            MDC.setContextMap(map);
         } else {
            MDC.clear();
         }
      }

      @Override
      public void onError(Throwable throwable) {
         coreSubscriber.onError(throwable);
      }

      @Override
      public Context currentContext() {
         return coreSubscriber.currentContext();
      }

      @Override
      public void onComplete() {
         coreSubscriber.onComplete();
      }
   }

}
