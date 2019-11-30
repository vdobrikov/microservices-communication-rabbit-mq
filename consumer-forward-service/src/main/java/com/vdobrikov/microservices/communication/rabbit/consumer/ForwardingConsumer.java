package com.vdobrikov.microservices.communication.rabbit.consumer;

import com.vdobrikov.microservices.communication.rabbit.messaging.Publisher;
import com.vdobrikov.microservices.communication.rabbit.messaging.Subscriber;
import com.vdobrikov.microservices.communication.rabbit.messaging.Wrapper;
import com.vdobrikov.microservices.communication.rabbit.messaging.exception.MessagingException;
import com.vdobrikov.microservices.communication.rabbit.messaging.rabbit.RabbitMQPublisher;
import com.vdobrikov.microservices.communication.rabbit.messaging.rabbit.RabbitMQSubscriber;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class ForwardingConsumer {
    private final Subscriber subscriber;
    private final Publisher publisher;
    private final String topic;
    private final String host;

    public ForwardingConsumer() throws IOException {
        Properties properties = new Properties();
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties")) {
            properties.load(is);
        }
        host = properties.getProperty("rabbitmq.host");
        topic = System.getenv("SUBSCRIBER_TOPIC");

        subscriber = new RabbitMQSubscriber(host);

        publisher = new RabbitMQPublisher(host);
    }

    public void subscribe() throws MessagingException {
        log.info("Subscribing to {}:{}", host, topic);
        subscriber.subscribe(topic, this::handle);
        log.info("Subscribed");
    }

    private Void handle(String topic, Wrapper messageWrapper) throws MessagingException {
        log.info("topic={} messageWrapper={}", topic, messageWrapper);
        String fwTopic = (String) messageWrapper.get(Wrapper.FORWARD_TOPIC);
        if (fwTopic == null || fwTopic.isEmpty()) {
            return null;
        }
        log.info("Forwarding to {}", fwTopic);
        publisher.publish(fwTopic, messageWrapper);
        return null;
    }

    public static void main(String[] args) throws IOException, MessagingException {
        ForwardingConsumer consumer = new ForwardingConsumer();
        consumer.subscribe();
    }
}
