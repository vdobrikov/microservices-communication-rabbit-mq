package com.vdobrikov.microservices.communication.rabbit.consumer;

import com.vdobrikov.microservices.communication.rabbit.messaging.Config;
import com.vdobrikov.microservices.communication.rabbit.messaging.Publisher;
import com.vdobrikov.microservices.communication.rabbit.messaging.Subscriber;
import com.vdobrikov.microservices.communication.rabbit.messaging.Wrapper;
import com.vdobrikov.microservices.communication.rabbit.messaging.exception.MessagingException;
import com.vdobrikov.microservices.communication.rabbit.messaging.kafka.KafkaConfig;
import com.vdobrikov.microservices.communication.rabbit.messaging.kafka.KafkaPublisher;
import com.vdobrikov.microservices.communication.rabbit.messaging.kafka.KafkaSubscriber;
import com.vdobrikov.microservices.communication.rabbit.messaging.rabbit.RabbitMQPublisher;
import com.vdobrikov.microservices.communication.rabbit.messaging.rabbit.RabbitMQSubscriber;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.util.UUID;

import static com.vdobrikov.microservices.communication.rabbit.messaging.rabbit.RabbitMQConfig.RABBITMQ_HOST;

@Slf4j
public class ForwardingConsumer {
    private final Subscriber subscriber;
    private final Publisher publisher;
    private final String topic;
    private final String group;
    private final String hosts;

    public ForwardingConsumer() {
        hosts = System.getenv("KAFKA_SERVERS");
        topic = System.getenv("SUBSCRIBER_TOPIC");
        group = System.getenv("SUBSCRIBER_GROUP");

        subscriber = new KafkaSubscriber(Config.build()
                .with(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, hosts)
                .with(ConsumerConfig.GROUP_ID_CONFIG, group)
                .with(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false));

        publisher = new KafkaPublisher(Config.build()
                .with(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, hosts));
    }

    public void subscribe() throws MessagingException {
        log.info("Subscribing to {}:{} group={}", hosts, topic, group);
        subscriber.subscribe(topic, this::handle);
        log.info("Subscribed");
    }

    private Void handle(String topic, Wrapper messageWrapper) throws MessagingException {
        log.info("topic={} messageWrapper={}", topic, messageWrapper);
        if ("error".equals(messageWrapper.get(Wrapper.OBJECT)) && !KafkaConfig.KAFKA_FAILED_TOPIC_VALUE.equals(topic)) {
            throw new RuntimeException("Consumer failure");
        }

        String fwTopic = (String) messageWrapper.get(Wrapper.FORWARD_TOPIC);
        if (fwTopic == null || fwTopic.isEmpty()) {
            return null;
        }
        log.info("Forwarding to {}", fwTopic);
        publisher.publish(fwTopic, messageWrapper);
        return null;
    }

    public static void main(String[] args) throws MessagingException {
        ForwardingConsumer consumer = new ForwardingConsumer();
        consumer.subscribe();
    }
}
