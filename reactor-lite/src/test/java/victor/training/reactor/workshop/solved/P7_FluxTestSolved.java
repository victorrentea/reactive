package victor.training.reactor.workshop.solved;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import victor.training.reactor.workshop.P7_FluxSolved;
import victor.training.reactor.workshop.P7_FluxTest;

public class P7_FluxTestSolved extends P7_FluxTest {
  @InjectMocks
  P7_FluxSolved workshopSolved;

  @BeforeEach
  final void setSolved() {
    workshop = workshopSolved;
  }

}
