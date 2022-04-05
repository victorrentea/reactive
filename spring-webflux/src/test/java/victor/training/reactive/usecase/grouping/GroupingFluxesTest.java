package victor.training.reactive.usecase.grouping;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

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
//      when(apis.apiA(anyInt())).thenReturn(Mono.empty());
//      when(apis.apiB(anyInt())).thenReturn(Mono.empty());

      // when
//      target.processMessageStream(Flux.just(-1)).subscribe(); // NEVER .subscribe in tests as it does NOT block the JUnit thread > the assertions start firing too early
      target.processMessageStream(Flux.just(-1)).blockOptional();

      // then
      Mockito.verifyNoMoreInteractions(apis);
   }

   @Test
   void apiA_and_B_areCalled_forOddNumbers() {
   }

   @Test
   void apiA_and_B_areCalled_inParallel() {
   }

   @Test
   void apiCisCalled_withAListOf3_after3EvenNumbers() {
   }

   @Test
   void apiCisCalled_withAListOf2_after2EvenNumbers_and500MillisElapsed() {
   }
}