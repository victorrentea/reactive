package victor.training.reactive.demo.pitfalls;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class MissingSignals {
   static class Customer {}

   class MySqlRepo {
      public Mono<Customer> findCustomer(int customerId) {
         log.info("Searching customer for id " + customerId);
         if (customerId > 0) {
            return Mono.just(new Customer());
         } else {
            // but 1 day, the ID not found in DB...
            return Mono.empty();
         }
      }
   }
   private MySqlRepo mySqlRepo = new MySqlRepo();

   class CassandraRepo {
      public Mono<Customer> save(Customer customer) {
         return Mono.just(customer); // success
      }
   }
   private CassandraRepo cassandraRepo = new CassandraRepo();

   public static class Redis {
      public Mono<Void> increaseApiUsageCounter(int customerId) {
         log.info("Increase counters for customer id " + customerId);
         return Mono.empty();
      }
   }
   private Redis redis = new Redis();


   // TODO fix the tests
   public Mono<Void> losingSignals(int customerId) {
      return mySqlRepo.findCustomer(customerId)
          .flatMap(c -> cassandraRepo.save(c))
          .flatMap(c -> redis.increaseApiUsageCounter(customerId))
          ;
   }


}
