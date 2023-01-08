package victor.training.reactor.workshop.solved;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import victor.training.reactor.workshop.P6_BridgeTest;

public class P6_BridgeTestSolved extends P6_BridgeTest {
  @InjectMocks
  P6_BridgeSolved workshopSolved;

  @BeforeEach
  final void setSolved() {
    workshop = workshopSolved;
  }

}
