package victor.training.reactive.assignment;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Slf4j
@Data
@Service
public class CustomerAuditService {

   private final EmailSender emailSender;
   private final CustomerReactiveRepo repo;
   @Autowired
   private String baseUrl;

   // TODO make tests happy
   public void processOrderingCustomer(Flux<Integer> customerIdFlux) {
   }

}
