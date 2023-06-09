package victor.training.reactor.workshop.solved;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import victor.training.reactor.workshop.C9_ComplexFlowTest;

public class C9_ComplexFlowTestSolved extends C9_ComplexFlowTest {
  @InjectMocks
  C9_ComplexFlowSolved workshopSolved;

  @BeforeEach
  final void setSolved() {
    workshop = workshopSolved;
  }

}
