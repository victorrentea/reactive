package victor.training.reactive;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockHound.Builder;
import reactor.blockhound.integration.BlockHoundIntegration;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.List;
import java.util.Scanner;
import java.util.ServiceLoader;

@Slf4j
public class Utils {
	
	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}		
	}

	public static void waitForEnter() {
		System.out.println("\nHit [ENTER] to continue");
		new Scanner(System.in).next();
	}

	public static void installBlockHound(List<Tuple2<String, String>> excludedClassMethods) {
		log.warn("Installing BlockHound to detect I/O in non-blocking threads");

		Builder builder = BlockHound.builder();
		ServiceLoader.load(BlockHoundIntegration.class).forEach(integration -> integration.applyTo(builder));
		for (Tuple2<String, String> classMethod : excludedClassMethods) {
			builder.allowBlockingCallsInside(classMethod.getT1(), classMethod.getT2());
		}
		builder.install();
	}


	public static Mono<ResponseEntity<Void>> refreshWireMockStubsFromJson() {
		return WebClient.create().post().uri("http://localhost:9999/__admin/mappings/reset").retrieve().toBodilessEntity();
	}
}
