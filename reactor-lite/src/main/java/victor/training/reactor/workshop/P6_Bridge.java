package victor.training.reactor.workshop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitResult;
import reactor.core.publisher.Sinks.One;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class P6_Bridge {

  protected Logger log = LoggerFactory.getLogger(P6_Bridge.class);

  static class User {
  }

  static class ResponseMessage {
  }

  interface Dependency {
    Mono<String> save(String message);

    Flux<String> findAll();

    String legacyBlockingCall(); // blocks the caller thread

    void sendMessage(long id);
  }

  protected final Dependency dependency;

  public P6_Bridge(Dependency dependency) {
    this.dependency = dependency;
  }


  // ==================================================================================
  // TODO call dependency#save and block the current thread until it completes, then log.info the returned value
  // Use-Case: hang a Kafka Listener thread that would otherwise consume a new message
  public void p01_blockForMono(String message) {
    String s = dependency.save(message).block(); // RAU! sa blochezi threadu curent. Cand are sens?
    log.info(s);
    // MOTIVU1: MQ listener ne reactive
    // codu asta e chemat dintr-un context non reactiv. eg Rabbit Listener
    // ce ar merge rau daca in loc sa fac .block() fac .subscribe() intr-un listener de Rabbit =>?
      // problema1(BIG): daca dau exceptie in save()? mai vede rabbitu eroare sa faca retry(3) la mesaj (sau sa-l puna in DLQ)
      // problema2(LOAD): te sufoca rabbitu: listeneru (tu) nu faci fata la cate mesaje iti impinge el. ca el iti da alt mesaj imediat daca ii spui "gata sefu" in 0,02 ms
//    dependency.save(message).doOnNext(s2-> log.info(s2)).subscribe();

    // MOTIVU1: cand esti intr-un @Scheduled : scheduled nu pune in exec acceeasi met daca inca ruleaza
  }


//  @GetMapping
//  public Flux<String> multeMaiEficient() { // pentru ca memorie:
//      // serverul poate trimite JSONurile pe rand element cu elemnent imediat ce vin
////    daca aduc de pe remote pe toti odata, nu prea am beneficiu, eg:
////    Mono<Parent> mp = WebClient...retrieve;
////    return mp.flatMapMany(parent -> parent.getChildren())
//    // dar e ma eficient daca faci de ex:
//    Flux<Date> date = reactiveRepo.findAllByUsername("u");
//    return date.map(Object::toString);
//  }
//  @GetMapping
//  public Mono<List<String>> multe() {
//
//  }

  // ==================================================================================
  // TODO call dependency#findAll() and block to get all values, then log.info the list in the console.
  // Use-Case: find all items to process in a table / write a test.
  public void p02_blockForFlux() {
    List<String> list = dependency.findAll().collectList().block();
    log.info("List:" + list);
  }



  // ==================================================================================
  // eg: legacyBlockingCall() {
  //    springDataJpa.findByGrupaDeSange("..."):Person !!! NU POTI FOLOSI HIBERNATE CU REACTOR!
  //    restTemplate.getFor... > a blocat threadu!
  //    .wsdl call > nu exista moduri reactive de a apela alea
  //    rabbit.send() daca vrei guaranteed delivery
  //    fisierDePeFtp.read() -> I/O
  //
  // TODO call dependency#legacyBlockingCall() and return its result in a Mono.
  //  NOTE: you are only allowed to block threads from Schedulers.boundedElastic()
  // In Reactor exista 4 scheduleri=thread pools: parallel (pt CPU), single (teste), netty(http), boundedElastic(pt a te bloca)
  public Mono<String> p03_blockingCalls() {
    log.info("Pe ce thread sunt aici si incerc sa ma blochez?");

    return Mono.fromCallable(() -> dependency.legacyBlockingCall())
            // asa faci o lambda sa ruleze pe ce Scheduler(aka thread pool) vrei
            .subscribeOn(Schedulers.boundedElastic());

  }

  // ==================================================================================
  // TODO Adapt Mono to Java 8+ CompletableFuture
  public CompletableFuture<User> p04_fromMonoToCompletableFuture(Mono<User> mono) {
    // Ce diferenta e intre Mono si CompletableFuture?
    // Asemanarea: ambele iti dau date mai tarziu
    // Diferenta: CompletableFuture.supplyAsync( [, executor custom]) -> commonPool daca nu-i dai executor
    // Diferenta: un Mono are nevoie sa-i faci subscribe; CompletableFuture se porneste imediat ce pune mana pe un thread din thread poolul pe care i-ai spus sa ruleze
    return mono.toFuture(); // DUR: face .subscribe
  }

  // ==================================================================================
  // TODO Adapt Java 8+ CompletableFuture to Mono
  public Mono<User> p05_fromCompletableFutureToMono(CompletableFuture<User> future) {
    return Mono.fromFuture(future); // futureul deja e in executie
  }
  // What is the difference between CompletableFuture and Mono? When do they start executing?






  // ==================================================================================
  // Bridgeul intre Reactive <> Callbacks
  // TODO call dependency#sendRequest(id) and return a mono
  //  that emits the Response received in the next method.
  // Use-Case: message bridge: send on a Kafka topic and receive the response on a second Kafka topic.
  // Hint: use Sinks.???
  @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Mono<ResponseMessage> p06_sendRequestOnQueueAndReturnAMonoThatWouldCompleteWhenResponseComesBackOn2ndResonseQueue(long id) {
    // rabbit.send(request)
    return Mono.fromRunnable(() -> dependency.sendMessage(id))
            .subscribeOn(Schedulers.boundedElastic())
            .then(pendingHttpRequests.computeIfAbsent(id,numipasa -> Sinks.one()).asMono())
            .timeout(Duration.ofMinutes(1))
            .doOnTerminate(()-> System.out.println("Oare am scos ceva? " + (pendingHttpRequests.remove(id)!=null)))

            ;
  }

  // Sinks iti permit sa EMITI programatic elemente pe un Mono/Flux de pe alta metoda, cand vrei tu.
  private Map<Long, One<ResponseMessage>> pendingHttpRequests = Collections.synchronizedMap(new HashMap<>());

  // @RabitListener...
  public void p06_receiveResponse(long id, ResponseMessage response) {
    // TODO write code here to send the response in the mono returned in the previous method.
    // This method is called once from tests 500ms after the first. Try to Publisher#log() signals to see for yourself.
    One<ResponseMessage> pendingHttpRequest = pendingHttpRequests.get(id);
    if (pendingHttpRequest == null) {
      log.warn("Rasp de la rabbit a venit prea tarziu. A plecat deja Bro'");
      return;
    }
    pendingHttpRequest.tryEmitValue(response);
  }
  // ⭐️ Challenge: Can you make t







  // ==================================================================================
  // TODO This method is called once. Every time the next method is called, this flux must emit the integer data.
  // Use-Case: report on a Flux signals received as API calls or Messages
  // Hint: use Sinks.???
  public Flux<Integer> p07_fluxOfSignals() {
    return null;
  }

  private Sinks.Many<Integer> fluxSink; // TODO = ...

  public void p07_externalSignal(Integer data) {
    // TODO write code here to emit the data in the flux returned in the previous method.
  }


}
