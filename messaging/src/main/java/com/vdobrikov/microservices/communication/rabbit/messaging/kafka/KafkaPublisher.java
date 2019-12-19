package com.vdobrikov.microservices.communication.rabbit.messaging.kafka;

import com.vdobrikov.microservices.communication.rabbit.messaging.Config;
import com.vdobrikov.microservices.communication.rabbit.messaging.Publisher;
import com.vdobrikov.microservices.communication.rabbit.messaging.Wrapper;
import com.vdobrikov.microservices.communication.rabbit.messaging.exception.MessagingException;
import com.vdobrikov.microservices.communication.rabbit.messaging.mapper.JsonMessageMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.IOException;
import java.util.Objects;

@Slf4j
public class KafkaPublisher implements Publisher {
    private final Producer<String, Wrapper> kafkaProducer;

    public KafkaPublisher(Config config) {
        kafkaProducer = new KafkaProducer<>(config, new StringSerializer(), new JsonSerializer(new JsonMessageMapper()));
    }

    @Override
    public void basicPublish(String topic, Object message) throws MessagingException {
        publish(topic, new Wrapper().withObject(message));
    }

    @Override
    public void publish(String topic, Wrapper message) throws MessagingException {
        Objects.requireNonNull(topic, "'topic' cannot be null");
        Objects.requireNonNull(message, "'message' cannot be null");

        String messageId = String.format("%s:%s", message.hashCode(), System.currentTimeMillis());

        log.info("Sending topic={} id={} message={}", topic, messageId, message);

        kafkaProducer.send(new ProducerRecord<>(topic, messageId, message), (recordMetadata, e) -> {
            if (e != null) {
                log.error("Filed to send message: topic={}, id={}", topic, messageId, e);
                return;
            }
            log.info("Message sent: topic={} id={} meta={}", topic, messageId, recordMetadata);
        });
    }

    @Override
    public void close() throws IOException {
        kafkaProducer.close();
    }
}
