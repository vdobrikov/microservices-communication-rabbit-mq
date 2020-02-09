package com.vdobrikov.microservices.communication.rabbit.messaging.rabbit;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.vdobrikov.microservices.communication.rabbit.messaging.Config;
import com.vdobrikov.microservices.communication.rabbit.messaging.exception.MessagingException;
import com.vdobrikov.microservices.communication.rabbit.messaging.mapper.JsonMessageMapper;
import com.vdobrikov.microservices.communication.rabbit.messaging.mapper.MessageMapper;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.vavr.CheckedFunction0;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Supplier;

import static com.vdobrikov.microservices.communication.rabbit.messaging.rabbit.RabbitMQConfig.RABBITMQ_DEFAULT_EXCHANGE;
import static com.vdobrikov.microservices.communication.rabbit.messaging.rabbit.RabbitMQConfig.RABBITMQ_DEFAULT_EXCHANGE_VALUE;
import static com.vdobrikov.microservices.communication.rabbit.messaging.rabbit.RabbitMQConfig.RABBITMQ_HOST;
import static com.vdobrikov.microservices.communication.rabbit.messaging.rabbit.RabbitMQConfig.RABBITMQ_MESSAGE_MAPPER;
import static com.vdobrikov.microservices.communication.rabbit.messaging.rabbit.RabbitMQConfig.RABBITMQ_MESSAGE_MAPPER_VALUE;


@Slf4j
public abstract class RabbitMQTransport implements Closeable {
    private static final Supplier<RetryConfig> DEFAULT_RETRY_CONFIG = () -> RetryConfig.custom()
            .maxAttempts(Integer.MAX_VALUE)
//            .retryOnException(e -> {log.warn("Retry on exception", e); return true;})
            .build();
    private static final Supplier<Retry> DEFAULT_RETRY = () -> Retry.of("default", DEFAULT_RETRY_CONFIG);

    protected final String exchangeName;
    protected MessageMapper messageMapper;

    private ConnectionFactory connectionFactory;
    private Channel channel;

    private final CheckedFunction0<Connection> getConnectionFn;
    private final CheckedFunction0<Channel> getChannelFn;

    public RabbitMQTransport(Config config) {
        String hostname = config.getString(RABBITMQ_HOST);
        Objects.requireNonNull(hostname, "'hostname' cannot be null");

        this.exchangeName = config.getStringOrDefault(RABBITMQ_DEFAULT_EXCHANGE, RABBITMQ_DEFAULT_EXCHANGE_VALUE);
        this.messageMapper = config.getOrDefaultGet(RABBITMQ_MESSAGE_MAPPER, RABBITMQ_MESSAGE_MAPPER_VALUE);

        this.connectionFactory = createConnectionFactory(hostname);
        this.getConnectionFn = Retry.decorateCheckedSupplier(DEFAULT_RETRY.get(), () -> connectionFactory.newConnection());
        this.getChannelFn = Retry.decorateCheckedSupplier(DEFAULT_RETRY.get(), () -> createConnection().createChannel());
    }

    protected Channel getChannel() throws MessagingException {
        if (channel == null) {
            channel = Try.of(getChannelFn).getOrElseThrow(e -> new MessagingException(e));
        }
        return channel;
    }

    private Connection createConnection() throws MessagingException {
        return Try.of(getConnectionFn).getOrElseThrow(e -> new MessagingException(e));
    }

    private static ConnectionFactory createConnectionFactory(String brokerHostname) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setAutomaticRecoveryEnabled(true);
        factory.setHost(brokerHostname);
        return factory;
    }

    @Override
    public void close() throws IOException {
        shutdown();
    }

    private void shutdown() {
        log.info("Shutdown..");
        try {
            getChannel().close();
        } catch (Exception e) {
            log.error("Failed to close channel");
        }
        log.info("Shutdown complete");
    }
}
