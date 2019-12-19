package com.vdobrikov.microservices.communication.rabbit.messaging.kafka;

import com.google.common.collect.EvictingQueue;
import com.vdobrikov.microservices.communication.rabbit.messaging.Config;
import com.vdobrikov.microservices.communication.rabbit.messaging.Subscriber;
import com.vdobrikov.microservices.communication.rabbit.messaging.Wrapper;
import com.vdobrikov.microservices.communication.rabbit.messaging.exception.MessagingException;
import com.vdobrikov.microservices.communication.rabbit.messaging.mapper.JsonMessageMapper;
import io.vavr.CheckedFunction2;
import io.vavr.CheckedRunnable;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.vdobrikov.microservices.communication.rabbit.messaging.kafka.KafkaConfig.KAFKA_FAILED_TOPIC_VALUE;

@Slf4j
public class KafkaSubscriber implements Subscriber {
    private static final long DURATION_POLL_MILLIS = 100;

    private final KafkaConsumer<String, Wrapper> consumer;
    private final Executor executor;
    private final AtomicBoolean stopped = new AtomicBoolean(false);
    private final String failedTopic;
    private final KafkaPublisher failedEventPublisher;
    private final EvictingQueue<String> idsCache = EvictingQueue.create(500);

    public KafkaSubscriber(Config config) {
        config = config.with(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false)
            .with(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, Integer.MAX_VALUE);
        this.consumer = new KafkaConsumer<>(config, new StringDeserializer(), new JsonDeserializer(new JsonMessageMapper()));
        this.executor = Executors.newSingleThreadExecutor();
        this.failedTopic = config.getStringOrDefault(KafkaConfig.KAFKA_FAILED_TOPIC, KAFKA_FAILED_TOPIC_VALUE);
        this.failedEventPublisher = new KafkaPublisher(config);
    }

    @Override
    public void subscribeBasic(String topic, CheckedFunction2<String, Object, Void> messageConsumer) throws MessagingException {
        subscribe(topic, (t, wrapper) -> messageConsumer.apply(t, wrapper.get(Wrapper.OBJECT)));
    }

    @Override
    public void subscribe(String topic, CheckedFunction2<String, Wrapper, Void> messageConsumer) throws MessagingException {
        Objects.requireNonNull(topic, "'topic' cannot be null");
        Objects.requireNonNull(messageConsumer, "'messageConsumer' cannot be null");

        consumer.subscribe(Collections.singleton(topic));

        executor.execute(() -> pollUpdates(messageConsumer));
    }

    private void pollUpdates(CheckedFunction2<String, Wrapper, Void> messageConsumer) {
        try {
            while (!stopped.get()) {
                ConsumerRecords<String, Wrapper> records = consumer.poll(Duration.ofMillis(DURATION_POLL_MILLIS));
                Map<ConsumerRecord<String, Wrapper>, Optional<Exception>> recordsToResults = StreamSupport.stream(records.spliterator(), false)
                        .peek(record -> {if (idsCache.contains(record.key())) {
                            log.warn("Duplicate record with provided key={}", record.key());
                        }})
                        .filter(record -> !idsCache.contains(record.key()))
                        .peek(record -> idsCache.add(record.key()))
                        .collect(Collectors.toMap(
                                record -> record,
                                record -> CompletableFuture.supplyAsync(() -> this.handleRecord(() -> messageConsumer.apply(record.topic(), record.value())))
                                        .join()));

                // Proceed passed records
//                recordsToResults.entrySet()
//                        .stream()
//                        .filter(recordToResult -> !recordToResult.getValue().isPresent())
//                        .map(Map.Entry::getKey)
//                        .forEach(this::handlePassed);

                // Proceed failed records
                recordsToResults.entrySet()
                        .stream()
                        .filter(recordToResult -> recordToResult.getValue().isPresent())
                        .map(Map.Entry::getKey)
                        .forEach(this::handleFailed);

                // Committing all records since failed ones were already consumed and redirected to failed topic
                consumer.commitSync();
            }
        } finally {
            consumer.close();
        }

    }

    private void handlePassed(ConsumerRecord<String, Wrapper> record) {
        Map<TopicPartition, OffsetAndMetadata> offsets = Collections.singletonMap(
                new TopicPartition(record.topic(), record.partition()), new OffsetAndMetadata(record.offset() + 1));

        consumer.commitAsync(offsets, (offsets1, e) -> Optional.ofNullable(e)
                .ifPresent(ex -> log.error("Kafka offset commit fail. {}", offsets1, ex)));
    }

    private void handleFailed(ConsumerRecord<String, Wrapper> record) {
        log.warn("Failed to consume record. Forwarding to '{}' topic. record={}", failedTopic, record);
        try {
            failedEventPublisher.publish(failedTopic, record.value());
        } catch (MessagingException e) {
            log.error("Failed to publish event to '{}' topic", failedTopic);
        }
    }

    private Optional<Exception> handleRecord(CheckedRunnable runnable) {
        try {
            runnable.run();
        } catch (Throwable e) {
            return Optional.of(new MessagingException(e));
        }
        return Optional.empty();
    }

    @Override
    public void close() throws IOException {
        stopped.set(true);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting for shutdown", e);
        }
        consumer.close();
    }
}
