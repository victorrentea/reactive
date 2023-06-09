package victor.training.reactor.workshop.solved;

import org.junit.jupiter.api.BeforeEach;
import victor.training.reactor.workshop.C1_CreationTest;

public class C1_CreationTestSolved extends C1_CreationTest {
  @BeforeEach
  final void setSolved() {
    workshop = new C1_CreationSolved();
  }
}
