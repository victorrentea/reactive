package victor.training.reactive.reactor.lite;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.function.Function;
import java.util.function.Supplier;

public class Part13Threads {

   //========================================================================================

   // TODO Run readTask on the Schedulers#boundedElastic, and return a mono of the returned value in UPPERCASE
   public Mono<String> subscribe(Supplier<String> readTask) {
      return null;
   }

   //========================================================================================

   // TODO Run readTask on the Schedulers#boundedElastic followed by the cpuTask on the Schedulers#parallel
   public Mono<Void> ioVsCpu(Runnable ioTask, Runnable cpuTask) {
      return null;
   }

   //========================================================================================

   // TODO Run readTask on the Schedulers#boundedElastic in parallel with the cpuTask ran on the Schedulers#parallel
   public Mono<Void> ioVsCpuParallel(Runnable ioTask, Runnable cpuTask) {
      return null;
   }


   //========================================================================================

   public interface RxService {
      Mono<String> readData();
      Mono<Integer> cpuTask(String data);
      Mono<Void> writeData(Integer i);
   }

   // TODO Run in order:
   // 1) readTask Schedulers#boundedElastic
   // 2) cpuTask on the Schedulers#parallel
   // 3) writeTask Schedulers#boundedElastic
   public Mono<?> threadHopping(RxService rxService) {
      return null;

   }

   //========================================================================================
   public interface BlockingService {
      String readData();
      Integer cpuTask(String data);
      void writeData(Integer i);
   }

   // TODO Same as above, but calling a with non-reactive APIs
   public Mono<?> threadHoppingBlockingApi(BlockingService service) {
      return null;
   }

}
