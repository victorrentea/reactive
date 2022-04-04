package victor.training.reactive.reactor.augmenting;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;


interface NameApi {

   Mono<String> fetchName(Long id);
}
interface AgeApi {

   Mono<Integer> fetchAge(Long id);
}
interface MinorApi {

   Mono<Void> notifyMinor(String name);
}
public class Augmenting {
   private NameApi nameApi;
   private AgeApi ageApi;
   private MinorApi minorApi;

   Flux<User> augment(List<Long> userIds) {
      return Flux.fromIterable(userIds)
          .flatMap(id -> Mono.zip(
                  nameApi.fetchName(id).onErrorReturn("UNDEFINED"),
                  ageApi.fetchAge(id),
                  (name, age) -> new User(id,name,age)) // Mono<User>
              .onErrorResume(t -> Mono.empty())
          )
          .doOnNext (user-> { if (user.age < 18) minorApi.notifyMinor(user.name).subscribe();});
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
