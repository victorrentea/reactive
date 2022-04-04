package victor.training.reactive.demo;

import org.apache.logging.log4j.util.Strings;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.stream.Stream;

public class ReactiveFiles {
   public static void main(String[] args) throws IOException, InterruptedException {

      Flux<String> stringFlux = openFluxFromFile(new File("pom.xml"));

      writeFluxToFile(stringFlux, new File("pom-upper.xml"));

      Thread.sleep(1000);
   }

   private static Flux<String> openFluxFromFile(File file) {
      return Flux.using(
          () -> Files.lines(file.toPath()),  // how to open the resource (Stream<String>)
          Flux::fromStream, // Convert the Stream<String> to Flux<String>
          Stream::close // how to close the resource (Stream<String>)
      )
          .filter(Strings::isNotBlank)
          .map(String::toUpperCase);
   }

   private static void writeFluxToFile(Flux<String> stringFlux, File out) throws FileNotFoundException {
      PrintWriter fileWriter = new PrintWriter(out);
      stringFlux
          .publishOn(Schedulers.boundedElastic())
          .log()
          .doFinally(signalType -> fileWriter.close())
          .subscribe(fileWriter::println);
   }
}
