package com.vdobrikov.microservices.communication.rabbit.messaging;

import com.vdobrikov.microservices.communication.rabbit.messaging.exception.MessagingException;

import java.io.Closeable;

public interface Publisher  extends Closeable {
    void basicPublish(String topic, Object message) throws MessagingException;
    void publish(String topic, Wrapper message) throws MessagingException;
}
