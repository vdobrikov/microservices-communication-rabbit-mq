package com.vdobrikov.microservices.communication.rabbit.messaging.rabbit;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.vdobrikov.microservices.communication.rabbit.messaging.Wrapper;
import com.vdobrikov.microservices.communication.rabbit.messaging.Publisher;
import com.vdobrikov.microservices.communication.rabbit.messaging.exception.MessagingClientException;
import com.vdobrikov.microservices.communication.rabbit.messaging.exception.MessagingException;
import com.vdobrikov.microservices.communication.rabbit.messaging.mapper.MessageMapper;

import java.io.IOException;
import java.util.Objects;

public class RabbitMQPublisher extends RabbitMQTransport implements Publisher {

    public RabbitMQPublisher(String hostname) {
        super(hostname);
    }

    public RabbitMQPublisher(String hostname, String exchangeName) {
        super(hostname, exchangeName);
    }

    public RabbitMQPublisher(String hostname, String exchangeName, MessageMapper messageMapper) {
        super(hostname, exchangeName, messageMapper);
    }

    @Override
    public void basicPublish(String topic, Object message) throws MessagingException {
        Objects.requireNonNull(topic, "'topic' cannot be null");
        Objects.requireNonNull(message, "'message' cannot be null");

        Wrapper wrapper = new Wrapper().withObject(message);
        publish(topic, wrapper);
    }

    @Override
    public void publish(String topic, Wrapper wrapper) throws MessagingException {
        Objects.requireNonNull(topic, "'topic' cannot be null");
        Objects.requireNonNull(wrapper, "'envelope' cannot be null");

        try {
            getChannel().exchangeDeclare(exchangeName, BuiltinExchangeType.TOPIC);
            getChannel().basicPublish(exchangeName, topic, createProps(), messageMapper.fromObject(wrapper));
        } catch (IOException e) {
            throw new MessagingClientException(e);
        }
    }

    private AMQP.BasicProperties createProps() {
        return new AMQP.BasicProperties.Builder()
                .contentType(messageMapper.getContentType())
                .type(Wrapper.class.getCanonicalName())
                .build();
    }


}
