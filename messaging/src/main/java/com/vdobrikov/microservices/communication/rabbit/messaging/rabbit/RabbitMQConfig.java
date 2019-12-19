package com.vdobrikov.microservices.communication.rabbit.messaging.rabbit;

import com.vdobrikov.microservices.communication.rabbit.messaging.Config;
import com.vdobrikov.microservices.communication.rabbit.messaging.mapper.JsonMessageMapper;
import com.vdobrikov.microservices.communication.rabbit.messaging.mapper.MessageMapper;

import java.util.function.Supplier;

public class RabbitMQConfig extends Config {
    public static final String RABBITMQ_HOST = "rabbitmq.host";

    public static final String RABBITMQ_DEFAULT_EXCHANGE = "rabbitmq.default.exchange";
    public static final String RABBITMQ_DEFAULT_EXCHANGE_VALUE = "default";

    public static final String RABBITMQ_MESSAGE_MAPPER = "rabbitmq.message.mapper";
    public static final Supplier<MessageMapper> RABBITMQ_MESSAGE_MAPPER_VALUE = JsonMessageMapper::new;
}
