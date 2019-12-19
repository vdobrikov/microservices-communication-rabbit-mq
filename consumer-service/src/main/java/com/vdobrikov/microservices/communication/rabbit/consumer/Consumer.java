package com.vdobrikov.microservices.communication.rabbit.consumer;

import com.vdobrikov.microservices.communication.rabbit.messaging.Config;
import com.vdobrikov.microservices.communication.rabbit.messaging.Subscriber;
import com.vdobrikov.microservices.communication.rabbit.messaging.exception.MessagingException;
import com.vdobrikov.microservices.communication.rabbit.messaging.kafka.KafkaConfig;
import com.vdobrikov.microservices.communication.rabbit.messaging.kafka.KafkaSubscriber;
import com.vdobrikov.microservices.communication.rabbit.messaging.rabbit.RabbitMQSubscriber;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;

import java.util.UUID;

import static com.vdobrikov.microservices.communication.rabbit.messaging.rabbit.RabbitMQConfig.RABBITMQ_HOST;

@Slf4j
public class Consumer {
    private final Subscriber subscriber;
    private final String topic;
    private final String group;
    private final String hosts;

    public Consumer() {
        hosts = System.getenv("KAFKA_SERVERS");
        topic = System.getenv("SUBSCRIBER_TOPIC");
        group = System.getenv("SUBSCRIBER_GROUP");

        subscriber = new KafkaSubscriber(Config.build()
                .with(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, hosts)
                .with(ConsumerConfig.GROUP_ID_CONFIG, group)
                .with(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false));
    }

    public void subscribe() throws MessagingException {
        log.info("Subscribing to {}:{} group={}", hosts, topic, group);
        subscriber.subscribeBasic(topic, (topic, message) -> {
            log.info("topic={} message={}", topic, message);
            return null;
        });
        log.info("Subscribed");
    }

    public static void main(String[] args) throws MessagingException {
        Consumer consumer = new Consumer();
        consumer.subscribe();
    }
}
