package victor.training.reactor.workshop.solved;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import victor.training.reactor.workshop.C7_FluxTest;

public class C7_FluxTestSolved extends C7_FluxTest {
  @InjectMocks
  C7_FluxSolved workshopSolved;

  @BeforeEach
  final void setSolved() {
    workshop = workshopSolved;
  }

}
