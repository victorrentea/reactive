package victor.training.reactive.demo.throttle;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

//public class InigoProblem {
//
//    public Mono<String> sol1() {
//        Scheduler myOwnLimited = Schedulers.newBoundedElastic(5, 100,  "throttling-th-pool");
//        return Mono.fromSupplier(() -> callToTheWeakSystem())
//                .subscribeOn(myOwnLimited);
//        // - is app-scoped, not cluster-ready
//        // - you kill threads (yeah...)
//
//        // solution2: resilience4j
//    }
//
//    public String callToTheWeakSystem() {
//        return WebClient..... .block();// block the thread
//    }
//}
