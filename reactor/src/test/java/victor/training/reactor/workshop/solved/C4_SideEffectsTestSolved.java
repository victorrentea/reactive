package victor.training.reactor.workshop.solved;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import victor.training.reactor.workshop.C4_SideEffectsTest;

public class C4_SideEffectsTestSolved extends C4_SideEffectsTest {
  @InjectMocks
  C4_SideEffectsSolved workshopSolved;

  @BeforeEach
  final void setSolved() {
    workshop = workshopSolved;
  }

}
