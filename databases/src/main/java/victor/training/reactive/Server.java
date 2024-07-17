package victor.training.reactive;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@RestController
public class Server {
  @PostMapping("process")
  public void process(@RequestBody List<Integer> elemente) {
    log.info("Procesez elementele: {}", elemente);
  }
}
