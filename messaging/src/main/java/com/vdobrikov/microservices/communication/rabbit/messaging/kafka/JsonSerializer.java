package com.vdobrikov.microservices.communication.rabbit.messaging.kafka;

import com.vdobrikov.microservices.communication.rabbit.messaging.Wrapper;
import com.vdobrikov.microservices.communication.rabbit.messaging.exception.MessagingRuntimeMappingException;
import com.vdobrikov.microservices.communication.rabbit.messaging.mapper.MessageMapper;
import io.vavr.control.Try;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class JsonSerializer implements Serializer<Wrapper> {
    private final MessageMapper mapper;

    public JsonSerializer(MessageMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        //no-op
    }

    @Override
    public byte[] serialize(String topic, Wrapper wrapper) {
        return serialize(topic, null, wrapper);
    }

    @Override
    public byte[] serialize(String topic, Headers headers, Wrapper data) {
        return Try.of(() -> mapper.fromObject(data)).getOrElseThrow(e -> new MessagingRuntimeMappingException(e));
    }

    @Override
    public void close() {
        //no-op
    }
}
