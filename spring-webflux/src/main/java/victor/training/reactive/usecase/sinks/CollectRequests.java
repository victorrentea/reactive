package victor.training.reactive.usecase.sinks;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
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

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;

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
                .bufferTimeout(20, ofMillis(2000))
                .flatMap(this::processPage)
                .subscribe();
    }

    @NotNull
    private Mono<Map<String, PlusCode>> processPage(List<AddressWithCallback> requestPage) {
        List<String> addresses = requestPage.stream()
                .map(AddressWithCallback::getAddress)
                .collect(Collectors.toList());
        return apis.getPlusCode(addresses)
                .timeout(ofSeconds(20))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("BU hai la masa ca murim aicea")))
                .doOnNext(resultMap -> requestPage.forEach(addressWithCallback -> addressWithCallback
                            .getCallback()
                            .tryEmitValue(resultMap.get(addressWithCallback.getAddress())).orThrow()))
                .doOnError(e -> requestPage.stream().map(AddressWithCallback::getCallback)
                        .forEach(one->one.emitError(e, emitDetails())));
    }

    @GetMapping
    public Mono<String> request() {//@RequestParam(defaultValue = "addr") String address) {
        String address = UUID.randomUUID().toString();
        One<PlusCode> callback = Sinks.one();
        AddressWithCallback request = new AddressWithCallback(address, callback);

        return Mono.fromRunnable(() -> objectMany.emitNext(request, emitDetails())) // retry whenever race
                .then(callback.asMono()) // this emits only when the callback I sent in the line above is called.
                .map(code -> "Address: " + address + " mapped to " + code.getCode())
                ;
    }

    @NotNull
    private static Sinks.EmitFailureHandler emitDetails() {
        return (signalType, emitResult) -> EmitResult.FAIL_NON_SERIALIZED == emitResult;
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