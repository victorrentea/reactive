//package victor.training.reactive.databases.cassandra;
//
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.core.io.Resource;
//import org.springframework.data.cassandra.core.cql2.Ordering;
//import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
//import org.springframework.data.cassandra.core.mapping.Column;
//import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
//import org.springframework.data.cassandra.core.mapping.Table;
//import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
//import org.springframework.data.cassandra.repository.config.EnableReactiveCassandraRepositories;
//import org.springframework.stereotype.Component;
//
//import java.util.HashSet;
//import java.util.Set;
//import java.util.UUID;
//
//@SpringBootApplication
//@RequiredArgsConstructor
//@EnableReactiveCassandraRepositories
//public class CassandraApp /*extends AbstractCassandraConfiguration*/  {
//   public static void main(String[] args) {
//       SpringApplication.run(CassandraApp.class, args);
//   }
//
//
////   @Bean
////   @Override
////   public CassandraClusterFactoryBean cluster() {
////      final CassandraClusterFactoryBean bean = super.cluster();
////      bean.setKeyspaceCreations( getKeyspaceCreations() );
////      bean.setContactPoints( contactPoints );
////      bean.setPort( PORT );
////      bean.setMetricsEnabled( false );
////      bean.setJmxReportingEnabled( false );
////      return bean;
////   }
//
//
////   @Override
////   protected String getKeyspaceName() {
////      return "victor";
////   }
////   @Bean
////   public CassandraCqlClusterFactoryBean cluster() {
////      CassandraCqlClusterFactoryBean bean = new CassandraCqlClusterFactoryBean();
////      bean.setKeyspaceCreations(getKeyspaceCreations());
////      bean.setContactPoints(NODES);
////      bean.setUsername(USERNAME);
////      bean.setPassword(PASSWORD);
////      return bean;
////   }
//
//   @Value("classpath:/init.cql")
//   private Resource initCql;
//
////   @Override
////   @Bean
////   public KeyspacePopulator keyspacePopulator() {
////      System.out.println("HELLO!");
////      return new ResourceKeyspacePopulator(initCql);
////   }
//}
//
//@Component
//@Slf4j
//@RequiredArgsConstructor
//class Play implements CommandLineRunner{
//   private final BookRepository bookRepository;
//
//
//   @Override
//   public void run(String... args) throws Exception {
//      bookRepository.save(new Book()).block();
//      System.out.println(bookRepository.findAll().toIterable());
//   }
//}
//
//
////@Repository
// interface BookRepository extends ReactiveCassandraRepository<Book, UUID> {
//   //
//}
//
//@Data
//@Table
//@NoArgsConstructor
//class Book {
//   @PrimaryKeyColumn(
//       name = "isbn",
//       ordinal = 2,
//       type = PrimaryKeyType.CLUSTERED,
//       ordering = Ordering.DESCENDING)
//   private UUID id;
//   @PrimaryKeyColumn(
//       name = "title", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
//   private String title;
//   @PrimaryKeyColumn(
//       name = "publisher", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
//   private String publisher;
//   @Column
//   private Set<String> tags = new HashSet<>();
//
//   public Book(String title, String publisher, Set<String> tags) {
//      this.title = title;
//      this.publisher = publisher;
//      this.tags = tags;
//   }
//}