package com.vdobrikov.microservices.communication.rabbit.consumer;

import com.vdobrikov.microservices.communication.rabbit.messaging.rabbit.RabbitMQSubscriber;
import com.vdobrikov.microservices.communication.rabbit.messaging.Subscriber;
import com.vdobrikov.microservices.communication.rabbit.messaging.exception.MessagingException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

@Slf4j
public class Consumer {
    private final Subscriber subscriber;
    private final String topic;
    private final String host;

    public Consumer() throws IOException {
        Properties properties = new Properties();
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties")) {
            Objects.requireNonNull(is, "Failed to load 'application.properties'");
            properties.load(is);
        }
        host = properties.getProperty("rabbitmq.host");
        topic = System.getenv("SUBSCRIBER_TOPIC");

        subscriber = new RabbitMQSubscriber(host);
    }

    public void subscribe() throws MessagingException {
        log.info("Subscribing to {}:{}", host, topic);
        subscriber.subscribeBasic(topic, (topic, message) -> {
            log.info("topic={} message={}", topic, message);
            return null;
        });
        log.info("Subscribed");
    }

    public static void main(String[] args) throws IOException, MessagingException {
        Consumer consumer = new Consumer();
        consumer.subscribe();
    }
}
