package victor.training.util;

import org.jooq.lambda.Unchecked;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.function.Supplier;

import static reactor.core.scheduler.Schedulers.parallel;

public class RunAsNonBlocking {

  /**
   * Together with the dependency blockhound-junit-platform, checks that you don't block() in the called method
   */
  public static <T> T nonBlocking(Supplier<Mono<T>> monoSupplier) {
    return Mono.defer(monoSupplier)
            .subscribeOn(parallel())
            .block(); // blockhound detects any blocking in threads of this scheduler
  }

  public static <T> T decorateAsNonBlocking(T objectUnderTest, Class<? super T> clazz) {
    Callback h = new MethodInterceptor() {
      @Override
      public Object intercept(Object o, Method method, Object[] parameters, MethodProxy methodProxy) throws Throwable {
        if (method.getReturnType().equals(Mono.class)) {
          Supplier<Mono<T>> supplier = Unchecked.supplier(() -> (Mono) method.invoke(objectUnderTest, parameters));
          return Mono.defer(supplier).subscribeOn(parallel());
        } else if (method.getReturnType().equals(Flux.class)) {
          Supplier<Flux<T>> supplier = Unchecked.supplier(() -> (Flux) method.invoke(objectUnderTest, parameters));
          return Flux.defer(supplier).subscribeOn(parallel());
        }
        return method.invoke(objectUnderTest, parameters);
      }
    };
    return (T) Enhancer.create(clazz, h);
  }

}
