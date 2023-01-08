package victor.training.reactor.workshop.solved;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import victor.training.reactor.workshop.P9_ComplexFlowTest;

public class P9_ComplexFlowTestSolved extends P9_ComplexFlowTest {
  @InjectMocks
  P9_ComplexFlowSolved workshopSolved;

  @BeforeEach
  final void setSolved() {
    workshop = workshopSolved;
  }

}
