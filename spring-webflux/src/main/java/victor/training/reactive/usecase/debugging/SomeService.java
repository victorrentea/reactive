package victor.training.reactive.usecase.debugging;

import org.springframework.stereotype.Service;

@Service
public class SomeService {
   public int logic(String integer) {
      return Integer.parseInt(integer);
   }
}
