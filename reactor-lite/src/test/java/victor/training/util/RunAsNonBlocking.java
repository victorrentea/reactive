package victor.training.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import reactor.blockhound.BlockHound;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.function.Supplier;

import static reactor.core.scheduler.Schedulers.parallel;

public class RunAsNonBlocking {

   // paired with the dependency blockhound-junit-platform, checks that you don't block() in the called method
   public static <T> Mono<T> runsNonBlocking(Supplier<Mono<T>> monoSupplier) {
     return Mono.defer(monoSupplier)
             .subscribeOn(parallel()); // blockhound detects any blocking in threads of this scheduler
   }

}
