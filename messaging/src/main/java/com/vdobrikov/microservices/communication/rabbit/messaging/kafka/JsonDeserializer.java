package com.vdobrikov.microservices.communication.rabbit.messaging.kafka;

import com.vdobrikov.microservices.communication.rabbit.messaging.Wrapper;
import com.vdobrikov.microservices.communication.rabbit.messaging.exception.MessagingRuntimeMappingException;
import com.vdobrikov.microservices.communication.rabbit.messaging.mapper.JsonMessageMapper;
import io.vavr.control.Try;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class JsonDeserializer implements Deserializer<Wrapper> {
    private final JsonMessageMapper mapper;

    public JsonDeserializer(JsonMessageMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        //no-op
    }

    @Override
    public Wrapper deserialize(String topic, byte[] data) {
        return deserialize(topic, null, data);
    }

    @Override
    public Wrapper deserialize(String topic, Headers headers, byte[] data) {
        return Try.of(() -> mapper.fromMessage(data, Wrapper.class))
                .getOrElseThrow(e -> new MessagingRuntimeMappingException(e));
    }

    @Override
    public void close() {
        //no-op
    }
}
