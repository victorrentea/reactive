package victor.training.reactive.usecase.grouping;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Slf4j
@ExtendWith(MockitoExtension.class)
class GroupingFluxesTest {
   @Mock
   Apis apis;
   @InjectMocks
   GroupingFluxes target;

   // solution:

   @Test
   void negative() {
   }

   @Test
   void odd() {
   }

   @Test
   void oddInParallel() {
   }
   
   @Test
   void even3() {
   }

   @Test
   void even2_bufferTimeout() {
   }
}