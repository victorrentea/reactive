//package victor.training.reactive.usecase.augmentoptional;
//
//import lombok.Value;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//import victor.training.reactive.assignment.Customer;
//
//import java.util.List;
//
//@Value
//class MemberCard {
//   int points;
//}
//@Value
//class CustomerProfile {
//   String name;
//   MemberCard memberCard;
//}
//
//interface CustomerProfileApi {
//   Mono<CustomerProfile> fetchName(Long customerId);
//}
//public class Augmenting {
//   private CustomerProfileApi nameApi;
//
//   Flux<User> augment(List<Customer> userIds) {
//      return Flux.fromIterable(userIds)
//          .flatMap(userId -> Mono.zip(
//              nameApi.fetchName(userId),
//              ageApi.fetchAge(userId),
//              (name, age) -> new User(userId, name, age)))
//          .doOnNext(user -> {
//             if (user.getAge() < 18)
//                minorApi.notifyMinor(user.getName()).subscribe();
//          })
//          ;
//   }
//
//
//}
//
//class User {
//   public final Long id;
//   public final String name;
//   public final int age;
//
//   User(Long id, String name, int age) {
//      this.id = id;
//      this.name = name;
//      this.age = age;
//   }
//
//   public String getName() {
//      return name;
//   }
//
//   public Long getId() {
//      return id;
//   }
//
//   public int getAge() {
//      return age;
//   }
//}
