package com.vdobrikov.microservices.communication.rabbit.messaging;

import com.vdobrikov.microservices.communication.rabbit.messaging.exception.MessagingException;
import io.vavr.CheckedFunction2;

import java.io.Closeable;

public interface Subscriber extends Closeable {
    void subscribeBasic(String topic, CheckedFunction2<String, Object, Void> messageConsumer) throws MessagingException;
    void subscribe(String topic, CheckedFunction2<String, Wrapper, Void> messageConsumer) throws MessagingException;
}
