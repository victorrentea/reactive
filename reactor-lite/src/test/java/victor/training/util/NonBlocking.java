package victor.training.util;


import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(RunAsNonBlockingExtension.class)
public @interface NonBlocking {
}
