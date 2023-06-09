package victor.training.reactor.workshop.solved;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import victor.training.reactor.workshop.C2_EnrichTest;

public class C2_EnrichTestSolved extends C2_EnrichTest {
  @InjectMocks
  C2_EnrichSolved workshopSolved;

  @BeforeEach
  final void setSolved() {
    workshop = workshopSolved;
  }

}
