package victor.training.reactor.workshop.solved;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import victor.training.reactor.workshop.P2_EnrichSolved;
import victor.training.reactor.workshop.P2_EnrichTest;

public class P2_EnrichTestSolved extends P2_EnrichTest {
  @InjectMocks
  P2_EnrichSolved workshopSolved;

  @BeforeEach
  final void setSolved() {
    workshop = workshopSolved;
  }

}
