package victor.training.reactive.kafka;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.SenderRecord;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SampleProducer {
    private static final Logger log = LoggerFactory.getLogger(SampleProducer.class.getName());

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String TOPIC = "my-topic";

    private final KafkaSender<Integer, String> sender;

    public SampleProducer(String bootstrapServers) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, "sample-producer");
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        sender = KafkaSender.create(SenderOptions.create(properties));
    }

    public void sendMessages(String topic, int count, CountDownLatch latch) throws InterruptedException {

        sender.send(Flux.range(1, count)
                .map(i -> SenderRecord.create(new ProducerRecord<>(topic, i, "Message_" + i), i)))
            .doOnError(e -> log.error("Send failed", e))
            .subscribe(record -> {
                RecordMetadata metadata = record.recordMetadata();
                Instant timestamp = Instant.ofEpochMilli(metadata.timestamp());
                System.out.printf("Message %d sent successfully, topic-partition=%s-%d offset=%d timestamp=%s\n",
                    record.correlationMetadata(),
                    metadata.topic(),
                    metadata.partition(),
                    metadata.offset(),
                    timestamp);
                latch.countDown();
            });
    }

    public void close() {
        sender.close();
    }

    public static void main(String[] args) throws Exception {
        int count = 20;
        CountDownLatch latch = new CountDownLatch(count);
        SampleProducer producer = new SampleProducer(BOOTSTRAP_SERVERS);
        producer.sendMessages(TOPIC, count, latch);
        latch.await(10, TimeUnit.SECONDS);
        producer.close();
    }
}