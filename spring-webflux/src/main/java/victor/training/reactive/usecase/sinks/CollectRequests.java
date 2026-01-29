package victor.training.reactive.usecase.sinks;

import jakarta.annotation.PostConstruct;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitResult;
import reactor.core.publisher.Sinks.Many;
import reactor.core.publisher.Sinks.One;

import java.util.UUID;

import static java.time.Duration.ofMillis;

@Slf4j
@RestController
@SpringBootApplication
public class CollectRequests {
   public static void main(String[] args) {
      SpringApplication.run(CollectRequests.class, args);
   }

   private final Many<AddressWithCallback> objectMany = Sinks.many().unicast()
       .onBackpressureError();

   @Autowired
   private Apis apis;

   @PostConstruct
   public void init() {
      objectMany.asFlux()
          .bufferTimeout(2, ofMillis(2000))
          .subscribe(requestPage -> Flux.fromIterable(requestPage)
              .map(AddressWithCallback::getAddress)
              .collectList()
              .flatMap(addressPage -> apis.getPlusCode(addressPage))
              .subscribe(resultMap -> {
                 for (AddressWithCallback addressWithCallback : requestPage) {
                    PlusCode plusCode = resultMap.get(addressWithCallback.getAddress());
                    log.info("Found code for " + addressWithCallback.getAddress() + " : " + plusCode);
                    addressWithCallback.getCallback().tryEmitValue(plusCode).orThrow();
                 }
              }));
   }

   @GetMapping
   public Mono<String> request() {//@RequestParam(defaultValue = "addr") String address) {
      String address = UUID.randomUUID().toString();
      One<PlusCode> callback = Sinks.one();
      AddressWithCallback request = new AddressWithCallback(address, callback);
      return Mono.fromRunnable(() -> objectMany.emitNext(request,
              (signalType, emitResult) -> EmitResult.FAIL_NON_SERIALIZED == emitResult)) // retry whenever race
          .then(callback.asMono())
          .map(code -> "Address: " + address + " mapped to " + code.getCode())
          ;
   }
}

@Value
class AddressWithCallback {
   String address;
   Sinks.One<PlusCode> callback;
}

@Value
class PlusCode {
   String code;
}