package victor.training.reactive.usecase.augmenting;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;


interface NameApi {
   Mono<String> fetchName(Long userId);
}
interface AgeApi {
   Mono<Integer> fetchAge(Long userId);
}
interface UnderAgeApi {
   Mono<Void> notifyUnderAge(String name);
}
public class Augmenting {
   private NameApi nameApi;
   private AgeApi ageApi;
   private UnderAgeApi underAgeApi;

   Flux<User> augment(List<Long> userIds) {
      Long userId = -1L;
      Mono<String> nameMono = nameApi.fetchName(userId);
      Mono<Integer> ageMono = ageApi.fetchAge(userId);
      int age = 15;
      if (age < 18) underAgeApi.notifyUnderAge("name");

      return Flux.empty();
   }


}

class User {
   public final Long id;
   public final String name;
   public final int age;

   User(Long id, String name, int age) {
      this.id = id;
      this.name = name;
      this.age = age;
   }
}
// SOLUTION:
//return Flux.fromIterable(userIds)
//    .flatMap(id -> Mono.zip(
//            nameApi.fetchName(id).onErrorReturn("UNDEFINED"),
//            ageApi.fetchAge(id),
//            (name, age) -> new User(id,name,age)) // Mono<User>
//        .onErrorResume(t -> Mono.empty())
//    )
//    .doOnNext (user-> { if (user.age < 18) minorApi.notifyMinor(user.name).subscribe();});
