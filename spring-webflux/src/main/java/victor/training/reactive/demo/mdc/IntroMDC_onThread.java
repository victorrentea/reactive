package victor.training.reactive.demo.mdc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class IntroMDC_onThread {
   private static final Logger log = LoggerFactory.getLogger(IntroMDC_onThread.class);

   public static void main(String[] args) {
      MDC.put("MDC_KEY", "requestId");
      log.debug("In caller");
      f();
   }

   private static void f() {
      log.debug("In called function");
   }
}
