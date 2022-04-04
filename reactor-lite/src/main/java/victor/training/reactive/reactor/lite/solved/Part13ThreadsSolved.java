package victor.training.reactive.reactor.lite.solved;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import victor.training.reactive.reactor.lite.Part13Threads;

import java.util.function.Supplier;

public class Part13ThreadsSolved extends Part13Threads {
   @Override
   public Mono<String> subscribe(Supplier<String> readTask) {
      return Mono.fromSupplier(readTask)
          .subscribeOn(Schedulers.boundedElastic())
          .map(String::toUpperCase);
   }

   @Override
   public Mono<Void> ioVsCpu(Runnable ioTask, Runnable cpuTask) {
      return Mono.fromRunnable(ioTask)
          .subscribeOn(Schedulers.boundedElastic())
          .publishOn(Schedulers.parallel())
          .then(Mono.fromRunnable(cpuTask));
   }

   @Override
   public Mono<Void> ioVsCpuParallel(Runnable ioTask, Runnable cpuTask) {
      Mono<Void> ioMono = Mono.<Void>fromRunnable(ioTask).subscribeOn(Schedulers.boundedElastic());
      Mono<Void> cpuMono = Mono.<Void>fromRunnable(cpuTask).subscribeOn(Schedulers.parallel());
      return Mono.zip(ioMono, cpuMono).then();
   }

   @Override
   public Mono<?> threadHopping(RxService rxService) {
      return rxService.readData()
          .subscribeOn(Schedulers.boundedElastic())
          .publishOn(Schedulers.parallel())
          .flatMap(s -> rxService.cpuTask(s))
          .publishOn(Schedulers.boundedElastic())
          .flatMap(i -> rxService.writeData(i));
   }

   @Override
   public Mono<?> threadHoppingBlockingApi(BlockingService service) {
      return Mono.fromCallable(() -> service.readData())
          .subscribeOn(Schedulers.boundedElastic())
          .publishOn(Schedulers.parallel())
          .flatMap(s -> Mono.fromCallable(() -> service.cpuTask(s)))
          .publishOn(Schedulers.boundedElastic())
          .flatMap(i -> Mono.fromRunnable(() -> service.writeData(i)));
   }
}
