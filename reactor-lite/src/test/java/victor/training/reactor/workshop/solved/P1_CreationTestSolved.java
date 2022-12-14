package victor.training.reactor.workshop.solved;

import org.junit.jupiter.api.BeforeEach;
import victor.training.reactor.workshop.P1_CreationSolved;
import victor.training.reactor.workshop.P1_CreationTest;

public class P1_CreationTestSolved extends P1_CreationTest {
  @BeforeEach
  final void setSolved() {
    workshop = new P1_CreationSolved();
  }
}
