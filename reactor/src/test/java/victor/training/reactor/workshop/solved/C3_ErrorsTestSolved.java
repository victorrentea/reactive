package victor.training.reactor.workshop.solved;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import victor.training.reactor.workshop.C3_ErrorsTest;

public class C3_ErrorsTestSolved extends C3_ErrorsTest {
  @InjectMocks
  C3_ErrorsSolved workshopSolved;

  @BeforeEach
  final void setSolved() {
    workshop = workshopSolved;
  }

}
