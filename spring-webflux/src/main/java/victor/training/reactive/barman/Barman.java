package victor.training.reactive.barman;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class Barman {
    private static final Logger log = LoggerFactory.getLogger(Barman.class);

    public Mono<Beer> pourBeer() {
        log.info("Start beer");
        if (true) {
            return Mono.error(new IllegalArgumentException("NO BEER!"));
        }

        // 1: pretend
        //      Utils.sleep(1000);
        //      Beer beer = new Beer("blond");

        // 2: blocking REST call
        //      Beer beer = new RestTemplate().getForEntity("http://localhost:9999/api/beer", Beer.class).getBody();
        Mono<Beer> beerMono = WebClient.create().get().uri("http://localhost:9999/api/beer").retrieve().bodyToMono(Beer.class);

        log.info("End beer");
        return beerMono;

        // 3: non-blocking REST call
        //      return new AsyncRestTemplate().exchange(...).completable()....;
    }

    public Mono<Vodka> pourVodka() {
        log.info("Start vodka");
//        SOAP service WSDL
        return Mono.fromSupplier(() -> someOldBlockingCodeYouHAVE_toCall())
                .subscribeOn(Schedulers.boundedElastic())
                ;

        //      Utils.sleep(200);  // imagine blocking DB call
//        log.info("End vodka");
//        return Mono.just(new Vodka());
    }

    private static Vodka someOldBlockingCodeYouHAVE_toCall() {
        Utils.sleep(1000); // imagine PortApi.call
        return new Vodka();
    }
}
