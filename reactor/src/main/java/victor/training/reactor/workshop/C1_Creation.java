package victor.training.reactor.workshop;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;

@Slf4j
public class C1_Creation {

  //========================================================================================
  // TODO Return a Mono that emits a "foo" value (DATA SIGNAL)
  public Mono<String> mono1_just() {
    return Mono.just("foo");
//    return "foo".asMono();
  }
  // fun String.asMono(): Mono<String> = Mono.just(this)

  //========================================================================================
  // TODO Return an empty Mono (COMPLETION SIGNAL)
  //   eg for reporting completion of a task not returning any data
  public Mono<String> mono2_empty() { // repo.findById(id) when not found
//    return Mono.just(null); // ❌ bad. NPE at construction
    return Mono.empty(); // == Optional.empty(); Never do Mono<Optional> = redundant wrapping
  }

  //========================================================================================
  // TODO Return a Mono of the data passed as parameter.
  //  NOTE: data can come null => if null, emit no DATA SIGNAL, but only COMPLETION.
  public Mono<String> mono3_optional(String data) {
//    return Mono.justOrEmpty(Optional.ofNullable(data));
    return Mono.justOrEmpty(data);
  }

  //========================================================================================
  // TODO Create a Mono that completes with an ERROR SIGNAL of IllegalStateException
  public Mono<String> mono4_error() {
    // ⭐️ if a method declared to return a Mono/Flux, it should never throw an exception,
    //    but return it as an error signal, like this:
    return Mono.error(new IllegalStateException());
  }

  //========================================================================================

  // TODO Return a Mono that never emits any signal (eg for testing)
  public Mono<String> mono5_noSignal() {
    return Mono.never();
  }


  // TODO ⭐️ CHALLENGE: to understand the signals,
  //   reimplement all the above mono* exercises using Mono.create(..)
  //   [AVOID IN PRODUCTION]

  //========================================================================================
  // TODO Create a Mono that emits "BOO" after 100ms since subscription
  public Mono<String> mono6_delayedData() {
    return Mono.just("BOO").delayElement(Duration.ofMillis(100));

  }

  //========================================================================================
  // TODO Create a Mono that emits LocalDateTime.now() when subscribed to.
  // Hint: do NOT use .just() -> now() should be called later, when subscribed!
  public Mono<LocalDateTime> mono7_fromCallable() {
//    return Mono.just(LocalDateTime.now()); // it's not the time at susbscription to the returned Mono
    return Mono.fromSupplier(() -> LocalDateTime.now());
  }

  //========================================================================================
  // TODO Create a Mono that emits only completion after 100ms (with no value)
  // NOTE: the return type is Mono<Void>, indicating there is no data emitted to subscriber.
  public Mono<Void> mono8_delayedCompletion() {
    // ⭐️ when a method has nothing to return, use Mono<Void> type that says "I will only report completion or error"
    // Mono.never will never emit anything, not even completion

//    return Mono.never(); // compiles. useful for testing

//    return Mono.empty(); // valid. no  data. emits completion immediately

    // ⚠️ any method calls network (IO) MUST return a Publisher (Mono/Flux) that your caller can "weave" in their own reactive flow.
    // pretend this method calls an api POST /report-fraud that returns nothing
    return Mono.delay(Duration.ofMillis(100))
//        // emits a 0 after 100ms
        .then();
    // discard the 0 data and only pass the completion signal (or error)
//    return Mono.<Void>empty().delayElement(Duration.ofMillis(100)).then(); // TODO explain the error in @Test
//    return Mono.<Void>empty().delaySubscription(Duration.ofMillis(100));

//    return Mono.defer(() -> functionReturingAMono()); // has to be invoked only when subscribed to TODO vrentea don't miss this
  }


  //========================================================================================

  // TODO Return a Flux that contains 2 values "foo" and "bar" without using an array or a collection
  public Flux<String> flux1_values() {
    return Flux.just("foo", "bar");
  }

  //========================================================================================

  // TODO Create a Flux from a List that contains 2 values "foo" and "bar"
  public Flux<String> flux2_fromList(List<String> list) {
//    return Flux.fromIterable(list);
    return Mono.just(List.of("foo", "bar")) // Mono<List<String>>
        .flatMapMany(Flux::fromIterable) // Flux<String>
        .collectList() // Mono<List<String>>
        .flatMapMany(Flux::fromIterable);
  }

  //========================================================================================
  // TODO Return an empty Flux
  public Flux<String> flux3_empty() {
//    return Flux.never();
    return Flux.empty();
  }

  //========================================================================================
  // TODO Create a Flux that emits an IllegalStateException ERROR SIGNAL
  public Flux<String> flux4_error() {
    return Flux.error(new IllegalStateException());
  }

  //========================================================================================
  // TODO Create a Flux that emits decreasing values from 9 to 0 every 100ms
  public Flux<Long> flux5_delayedElements() {
    return Flux.interval(Duration.ofMillis(100))
        .take(10) // 0,1,2,3,4,5,6,7,8,9
        .map(i -> 9 - i) // in-mem instant(no IO) transformation

        // require full collection:
//        .reverse❌
//        .sort()✅
//        .distinct()//✅
        ;

//    return Flux.range(0, 10)
//        .delayElements(Duration.ofMillis(100))
//        .map(n-> 9L - n);
  }

  //========================================================================================
  // TODO print to console all signals going up (from Subscriber->Publisher)
  //  or down (from Publisher->Subscriber)
  public Flux<String> logSignals(Flux<String> flux) {
    return flux
        .log("stage1");
  }

  //========================================================================================
  // TODO print to console:
  //  - "SUBSCRIBE" on subscribe signal
  //  - "NEXT "+element on next signal (concatenate the element to message)
  //  - "ERROR" on error signal
  //  - "COMPLETE" on completion signal
  //  - "END" on error or⚠️ completion signal
  public Flux<String> doOnHooks(Flux<String> flux) {
    return flux
        .doOnSubscribe(sub -> System.out.println("SUBSCRIBE "+sub))
        .doOnNext(element -> System.out.println("NEXT " + element))
        .doOnError(error -> System.out.println("ERROR " + error))
        .doOnComplete(() -> System.out.println("COMPLETE"))
        .doOnTerminate(() -> System.out.println("END"))
        ;
  }

  //========================================================================================
  // TODO The returned Mono should emit the value "Hi " + the current username
  //  got from the Reactor Context, eg reactorContext.get("username")
  // Hint: Mono.deferContextual allows access to context propagated from downstream subscriber: inspect the test also.
  public Mono<String> reactorContext_read() {

    return Mono.deferContextual(context-> Mono.just("Hi " + context.get("username")))

        // log.info any element going through here along with the tenantId from the context
//        .doOnNext(e->log.info(e))

        .doOnEach(this::cleanCode) // ✅ work, as the context moves "UP WITH THE SUBSCRIBE SIGNAL"
        .contextWrite(context -> context.put("tenant","12")) // add to reactor context information
//        .doOnEach(this::cleanCode) // ❌ fails here
        ;
  }
    //⭐️⭐️ only use immutable objects/collections with reactive chains!

  private void cleanCode(Signal<String> signal) {
    if (signal.isOnNext()) {
      String tenant = signal.getContextView().get("tenant");
      log.info("Saw " + signal.get() + " from " + tenant);
    }
  }
  // propagating flow metadata:
  // A) in blocking (spring-web) using ThreadLocal data:
    // 1 Security Principal: SecurityContextHolder.getContext().getAuthentication()
    // 2 JDBC Connection ± open transaction = @Transactional
    // 3 MDC logging context
    //    MDC.put("tenantId","extracted from JWT/request header"); // in logging pattern %X{tenantId}
    // 4 TraceId for distributed tracing OTEL agent
    // after you set on threadlocal a value on that thread you can read back that value

  // B) in reactive chains (spring-webflux) using reactor context
  //  reactor context propagates upwards from subscriber to publisher along with the subscribe signal

//  record DataSpecificToEachThread(String username){}
//  static ThreadLocal<DataSpecificToEachThread> threadLocal = new ThreadLocal<>();
}
