package victor.training.reactive.demo.spring;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@RestController
public class LargeHttp {

   @GetMapping(value = "flood/generate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
   public Flux<DataItem> flood() {
      return Flux.<DataItem>generate(sink -> sink.next(generateItem()))
          .take(100_000_000);
   }

   public DataItem generateItem() {
      return new DataItem("Time is " + LocalDateTime.now());
   }

   @GetMapping("flood/download")
   public Flux<String> download() throws IOException {

      Writer fileWriter = new FileWriter("download.dat");
      return WebClient.create().get().uri("http://localhost:8080/flood/generate")
          .retrieve()
          .bodyToFlux(DataItem.class)
          .doOnNext(data -> {
             try {
                fileWriter.write(data.getValue() + "\n");
             } catch (IOException e) {
                throw new RuntimeException(e);
             }
          })
          .doFinally(signalType -> {
             try {
                fileWriter.close();
             } catch (IOException e) {
                e.printStackTrace();
             }
          })
          .scan(0, (sum, x) -> sum + 1)
          .sample(Duration.ofMillis(500))
          .map(i -> i + "<br>");
   }

   @Data
   @AllArgsConstructor
   @NoArgsConstructor
   public static class DataItem {
      private String value;
   }
}
