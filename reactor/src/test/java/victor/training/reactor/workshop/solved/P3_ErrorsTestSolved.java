package victor.training.reactor.workshop.solved;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import victor.training.reactor.workshop.P3_ErrorsTest;

public class P3_ErrorsTestSolved extends P3_ErrorsTest {
  @InjectMocks
  P3_ErrorsSolved workshopSolved;

  @BeforeEach
  final void setSolved() {
    workshop = workshopSolved;
  }

}
