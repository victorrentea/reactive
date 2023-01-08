package victor.training.reactor.workshop.solved;

import org.junit.jupiter.api.BeforeEach;
import victor.training.reactor.workshop.P5_TestingSolved;
import victor.training.reactor.workshop.P5_TestingTest;

public class P5_TestingTestSolved extends P5_TestingTest {
  @BeforeEach
  final void setSolved() {
    workshop = new P5_TestingSolved();
  }

}
