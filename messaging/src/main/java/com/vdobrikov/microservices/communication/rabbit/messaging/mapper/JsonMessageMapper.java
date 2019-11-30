package com.vdobrikov.microservices.communication.rabbit.messaging.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vdobrikov.microservices.communication.rabbit.messaging.exception.MessagingMappingException;
import io.vavr.control.Try;

public class JsonMessageMapper implements MessageMapper {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String getContentType() {
        return "application/json";
    }

    @Override
    public byte[] fromObject(Object object) throws MessagingMappingException {
        return Try.of(() -> MAPPER.writeValueAsBytes(object))
                .getOrElseThrow(e -> new MessagingMappingException(e));
    }

    @Override
    public <T> T fromMessage(byte[] message, Class<T> clazz) throws MessagingMappingException {
        return Try.of(() -> MAPPER.readValue(message, clazz))
                .getOrElseThrow(e -> new MessagingMappingException(e));
    }
}
