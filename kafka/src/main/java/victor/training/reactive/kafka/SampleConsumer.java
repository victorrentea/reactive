package victor.training.reactive.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SampleConsumer {
   private static final Logger log = LoggerFactory.getLogger(SampleConsumer.class.getName());

   private static final String BOOTSTRAP_SERVERS = "localhost:9092";
   private static final String TOPIC = "my-topic";

   private final ReceiverOptions<Integer, String> receiverOptions;

   public SampleConsumer(String bootstrapServers) {
      Map<String, Object> props = new HashMap<>();
      props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
      props.put(ConsumerConfig.CLIENT_ID_CONFIG, "sample-consumer");
      props.put(ConsumerConfig.GROUP_ID_CONFIG, "sample-group");
      props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
      props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
      props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
      receiverOptions = ReceiverOptions.create(props);
   }

   public Disposable consumeMessages(String topic, CountDownLatch latch) {
      ReceiverOptions<Integer, String> options = receiverOptions.subscription(Collections.singleton(topic))
          .addAssignListener(partitions -> log.debug("Partitions Assigned: {}", partitions))
          .addRevokeListener(partitions -> log.debug("Partitions Revoked: {}", partitions));
      Flux<ReceiverRecord<Integer, String>> kafkaFlux = KafkaReceiver.create(options)

          .receive()


          .doOnNext(this::logReceivedRecord);

      return kafkaFlux.subscribe(record -> {
         record.receiverOffset().acknowledge();
         latch.countDown();
      });
   }

   private void logReceivedRecord(ReceiverRecord<Integer, String> record) {
      ReceiverOffset offset = record.receiverOffset();
      Instant timestamp = Instant.ofEpochMilli(record.timestamp());
      String recordTime = timestamp.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_TIME);
      log.debug("Received message " +
                "(topic-partition=" + offset.topicPartition() +
                " offset=" + offset.offset() +
                " timestamp=" + recordTime + ") " +
                "key=" + record.key() + "\n" +
                record.value());
   }

   public static void main(String[] args) throws Exception {
      int count = 20;
      CountDownLatch latch = new CountDownLatch(count);
      SampleConsumer consumer = new SampleConsumer(BOOTSTRAP_SERVERS);
      Disposable disposable = consumer.consumeMessages(TOPIC, latch);
      latch.await(10, TimeUnit.SECONDS);
      disposable.dispose();
   }
}