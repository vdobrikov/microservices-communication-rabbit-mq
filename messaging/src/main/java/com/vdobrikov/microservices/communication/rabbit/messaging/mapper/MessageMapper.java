package com.vdobrikov.microservices.communication.rabbit.messaging.mapper;

import com.vdobrikov.microservices.communication.rabbit.messaging.exception.MessagingMappingException;

public interface MessageMapper {
    String getContentType();
    byte[] fromObject(Object object) throws MessagingMappingException;
    <T> T fromMessage(byte[] message, Class<T> clazz) throws MessagingMappingException;
}
