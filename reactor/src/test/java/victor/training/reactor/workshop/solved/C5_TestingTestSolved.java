package victor.training.reactor.workshop.solved;

import org.junit.jupiter.api.BeforeEach;
import victor.training.reactor.workshop.C5_TestingTest;

public class C5_TestingTestSolved extends C5_TestingTest {
  @BeforeEach
  final void setSolved() {
    workshop = new C5_TestingSolved();
  }

}
