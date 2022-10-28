package victor.training.reactive.databases;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import victor.training.reactive.databases.r2dbc.User;
import victor.training.reactive.databases.r2dbc.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
public class MyTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TransactionalOperator operator;
    @Autowired
    private ReactiveTransactionManager rwriteTransactionManager;

    @Test
    @Order(1)
    void explore() {
        assertThat(userRepository.count().block()).isEqualTo(0);
        TransactionalOperator.create(rwriteTransactionManager)
                .execute(status -> {
                    status.setRollbackOnly();
                    return userRepository.save(new User("Jhon"));
                }).blockFirst();

    }

    @Order(2)
    @Test
    void explore2() {
        assertThat(userRepository.count().block()).isEqualTo(0);
        userRepository.save(new User("Jhon")).block();
    }
}
