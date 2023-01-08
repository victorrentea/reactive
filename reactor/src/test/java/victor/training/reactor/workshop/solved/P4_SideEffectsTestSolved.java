package victor.training.reactor.workshop.solved;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import victor.training.reactor.workshop.P4_SideEffectsTest;

public class P4_SideEffectsTestSolved extends P4_SideEffectsTest {
  @InjectMocks
  P4_SideEffectsSolved workshopSolved;

  @BeforeEach
  final void setSolved() {
    workshop = workshopSolved;
  }

}
