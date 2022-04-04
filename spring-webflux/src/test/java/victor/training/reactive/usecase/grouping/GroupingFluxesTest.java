package victor.training.reactive.usecase.grouping;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GroupingFluxesTest {
   @Mock Apis apis;
   @InjectMocks GroupingFluxes target;

   @Test
   void processMessageStream() {

   }
}