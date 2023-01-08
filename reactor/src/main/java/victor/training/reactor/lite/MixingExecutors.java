package victor.training.reactor.lite;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class MixingExecutors {
   public static void main(String[] args) {

      // TODO call ioRead and ioWrite on boundedElastic scheduler
      // TODO call cpu on parallel scheduler

      // threads from ForkJoinPool.commonPool are daemon threads. the process dies if main exits
      Utils.waitForEnter();
   }

   public static int ioRead() {
      log.info("read");
      Utils.sleep(100);
      return 1;
   }

   public static int cpu(int i) {
      log.info("CPU");
      return i * 2;
   }

   public static void ioWrite(int i) {
      log.info("write " + i);
      Utils.sleep(100);
      log.info("Done");
   }
}
