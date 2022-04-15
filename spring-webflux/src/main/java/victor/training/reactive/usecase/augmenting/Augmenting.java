package victor.training.reactive.usecase.augmenting;

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

   //You are given a small list of user ids.
   //For each id, retrieve the name from NameApi and age from AgeApi.
   //For each user of age < 18, send a notification to MinorApi with its name
   //Return the list of User{id,name,age}

   Flux<User> augment(List<Long> userIds) {
//      Mono<String> stringMono = nameApi.fetchName(1l);
//      Mono<Integer> integerMono = ageApi.fetchAge(1l);

      return Flux.fromIterable(userIds)
          .flatMap(userId -> Mono.zip(
              nameApi.fetchName(userId),
              ageApi.fetchAge(userId),
              (name, age) -> new User(userId, name, age)))
          .doOnNext(user -> {
             if (user.getAge() < 18)
                minorApi.notifyMinor(user.getName()).subscribe();
          })
          ;
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

   public String getName() {
      return name;
   }

   public Long getId() {
      return id;
   }

   public int getAge() {
      return age;
   }
}
