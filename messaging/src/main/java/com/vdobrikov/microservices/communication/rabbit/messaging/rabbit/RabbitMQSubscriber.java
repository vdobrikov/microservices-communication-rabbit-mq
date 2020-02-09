package com.vdobrikov.microservices.communication.rabbit.messaging.rabbit;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.vdobrikov.microservices.communication.rabbit.messaging.Config;
import com.vdobrikov.microservices.communication.rabbit.messaging.Subscriber;
import com.vdobrikov.microservices.communication.rabbit.messaging.Wrapper;
import com.vdobrikov.microservices.communication.rabbit.messaging.exception.MessagingClientException;
import com.vdobrikov.microservices.communication.rabbit.messaging.exception.MessagingException;
import com.vdobrikov.microservices.communication.rabbit.messaging.exception.MessagingMappingException;
import io.vavr.CheckedFunction2;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;

@Slf4j
public class RabbitMQSubscriber extends RabbitMQTransport implements Subscriber {

    public RabbitMQSubscriber(Config config) {
        super(config);
    }

    @Override
    public void subscribeBasic(String topic, CheckedFunction2<String, Object, Void> messageConsumer) throws MessagingException {
        Objects.requireNonNull(topic, "'topic' cannot be null");
        Objects.requireNonNull(messageConsumer, "'messageConsumer' cannot be null");

        subscribe(topic, (t, wrapper) -> messageConsumer.apply(topic, wrapper.get(Wrapper.OBJECT)));
    }

    @Override
    public void subscribe(String topic, CheckedFunction2<String, Wrapper, Void> messageConsumer) throws MessagingException {
        Objects.requireNonNull(topic, "'topic' cannot be null");
        Objects.requireNonNull(messageConsumer, "'messageConsumer' cannot be null");

        try {
            String queueName = getChannel().queueDeclare().getQueue();
            getChannel().queueBind(queueName, exchangeName, topic);
            getChannel().basicConsume(queueName, new DefaultConsumer(getChannel()) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String contentType = properties.getContentType();
                    String type = properties.getType();
                    try {
                        assureContentType(contentType);
                        assureWrapperType(type);

                        Wrapper wrapper = messageMapper.fromMessage(body, Wrapper.class);
                        messageConsumer.apply(topic, wrapper);
                        getChannel().basicAck(envelope.getDeliveryTag(), false);
                    } catch (Throwable e) {
                        log.error("Failed to consume message", e);
                        getChannel().basicNack(envelope.getDeliveryTag(), false, true);
                    }
                }
            });
        } catch (IOException e) {
            throw new MessagingClientException(e);
        }
    }

    private void assureContentType(String type) throws MessagingMappingException {
        if (!messageMapper.getContentType().equalsIgnoreCase(type)) {
            throw new MessagingMappingException("Unknown content type: " + type);
        }
    }

    private void assureWrapperType(String type) throws MessagingMappingException {
        if (!Wrapper.class.getCanonicalName().equals(type)) {
            throw new MessagingMappingException("Unknown wrapper type: " + type);
        }
    }
}
