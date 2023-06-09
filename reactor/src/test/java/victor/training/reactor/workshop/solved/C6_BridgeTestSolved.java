package victor.training.reactor.workshop.solved;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import victor.training.reactor.workshop.C6_BridgeTest;

public class C6_BridgeTestSolved extends C6_BridgeTest {
  @InjectMocks
  C6_BridgeSolved workshopSolved;

  @BeforeEach
  final void setSolved() {
    workshop = workshopSolved;
  }

}
