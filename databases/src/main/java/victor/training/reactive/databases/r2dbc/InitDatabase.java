package victor.training.reactive.databases.r2dbc;

import io.r2dbc.spi.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.Arrays.asList;

@Slf4j
@Component
@RequiredArgsConstructor
public class InitDatabase implements CommandLineRunner {
   private final UserRepository userRepository;
   private final ConnectionFactory connectionFactory;

   @Override
   public void run(String... args) {
      log.info("Start init relational database");
      DatabaseClient databaseClient = DatabaseClient.create(connectionFactory);
      List<String> sqlLines = asList(
          "DROP TABLE IF EXISTS users;",
          "CREATE TABLE users (id serial primary key, name varchar);"
      );
      for (String sql : sqlLines) {
         log.info("Executing {} ...", sql);
         databaseClient
             .sql(sql)
             .fetch()
             .rowsUpdated()
             .block();
      }
      log.info("INSERT User");
      userRepository.save(new User( "test")).log().subscribe();
      log.info("INSERT User");
      userRepository.findAll().log().subscribe(System.out::println);
      log.info("done");
   }
}
