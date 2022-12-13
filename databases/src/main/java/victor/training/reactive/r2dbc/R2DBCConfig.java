package victor.training.reactive.r2dbc;

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@Configuration
@EnableR2dbcRepositories
public class R2DBCConfig extends AbstractR2dbcConfiguration {

  @Value("${datasource.host}")
  private String host;
  @Value("${datasource.port}")
  private int port;
  @Value("${datasource.database}")
  private String database;
  @Value("${datasource.username}")
  private String username;
  @Value("${datasource.password}")
  private String password;

  @Bean
  @Override
  public ConnectionFactory connectionFactory() {
    return new PostgresqlConnectionFactory(PostgresqlConnectionConfiguration
        .builder()
        .host(host)
        .database(database)
        .username(username)
        .password(password)
        .port(port)
        .build());
  }


}



