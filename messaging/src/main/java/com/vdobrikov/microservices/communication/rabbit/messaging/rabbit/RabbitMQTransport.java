package com.vdobrikov.microservices.communication.rabbit.messaging.rabbit;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
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

@Slf4j
public abstract class RabbitMQTransport implements Closeable {
    private final static String DEFAULT_EXCHANGE_NAME = "default";
    private static final Supplier<MessageMapper> DEFAULT_MESSAGE_MAPPER = JsonMessageMapper::new;
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

    public RabbitMQTransport(String hostname) {
        this(hostname, DEFAULT_EXCHANGE_NAME);
    }

    public RabbitMQTransport(String hostname, String exchangeName) {
        this(hostname, exchangeName, DEFAULT_MESSAGE_MAPPER.get());
    }

    public RabbitMQTransport(String hostname, String exchangeName, MessageMapper messageMapper) {
        Objects.requireNonNull(hostname, "'hostname' cannot be null");
        Objects.requireNonNull(exchangeName, "'exchangeName' cannot be null");
        Objects.requireNonNull(messageMapper, "'messageMapper' cannot be null");

        this.exchangeName = exchangeName;
        this.connectionFactory = createConnectionFactory(hostname);
        this.getConnectionFn = Retry.decorateCheckedSupplier(DEFAULT_RETRY.get(), () -> connectionFactory.newConnection());
        this.getChannelFn = Retry.decorateCheckedSupplier(DEFAULT_RETRY.get(), () -> createConnection().createChannel());
        this.messageMapper = messageMapper;
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
