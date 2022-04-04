package victor.training.reactive.demo.pitfalls;

import lombok.RequiredArgsConstructor;
import org.jooq.lambda.fi.util.function.CheckedConsumer;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class RaceArguments {
   @RequiredArgsConstructor
   class Foo {
      private final UploadService service;

      Mono<Void> tryUpload(InputStream in) {
         return service.doUpload(in).doFinally(CheckedConsumer.unchecked(x -> in.close()));
      }

      Mono<Void> problem(byte[] data) {
         return tryUpload(new BufferedInputStream(new ByteArrayInputStream(data)))
             .doOnError(t -> System.out.println("Saw " + t))
             .retry(5);
      }
   }

   static class UploadService {
      boolean firstRun = true;
      Mono<Void> doUpload(InputStream in) {
         return Mono.defer(() -> {
            if (firstRun) {
               firstRun = false;
               return Mono.error(new IOException("Test"));
            }

            System.out.println("Trying");

            try {
               Files.copy(in, new File("out.txt").toPath(), StandardCopyOption.REPLACE_EXISTING);
               return Mono.empty();
            } catch (IOException e) {
               return Mono.error(e);
            }
         });
      }
   }


   @Test
   public void test() throws InterruptedException, IOException {
      byte[] arr = "Halo".getBytes(StandardCharsets.UTF_8);
//      InputStream vais = new BufferedInputStream(new ByteArrayInputStream(arr));
//      vais.close();
//      Files.copy(vais, new File("out.txt").toPath(), StandardCopyOption.REPLACE_EXISTING);

//      UploadService uploadService = Mockito.mock(UploadService.class);
//      when(uploadService.doUpload(any()))
//          .thenReturn(Mono.error(new IllegalArgumentException()))
//          .thenReturn(Mono.empty());
      Foo foo = new Foo(new UploadService());

      foo.problem(arr).subscribe();
      Thread.sleep(2000);
   }

}

