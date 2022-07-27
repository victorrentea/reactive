package victor.training.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import reactor.blockhound.BlockHound;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;

import static reactor.core.scheduler.Schedulers.parallel;

@Slf4j
public class RunAsNonBlockingExtension implements InvocationInterceptor {

   @Override
   public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
      if (!invocationContext.getExecutable().isAnnotationPresent(NonBlocking.class)) {
         invocation.proceed();
      } else {
         log.warn("Installing BlockHound to detect blocking code [irreversible operation for this JVM] ...");
         BlockHound.install();

         Mono.fromRunnable(() -> {
                try {
                   invocation.proceed();
                } catch (Throwable e) {
                   throw new RuntimeException(e);
                }
             }).subscribeOn(parallel()) // run the test in a non-blocking thread that BlockHound will monitor
             .block();
      }
   }

}
