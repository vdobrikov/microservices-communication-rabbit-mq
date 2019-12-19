package com.vdobrikov.microservices.communication.rabbit.messaging.exception;

public class MessagingRuntimeMappingException extends MessagingRuntimeException {
    public MessagingRuntimeMappingException() {
    }

    public MessagingRuntimeMappingException(String message) {
        super(message);
    }

    public MessagingRuntimeMappingException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessagingRuntimeMappingException(Throwable cause) {
        super(cause);
    }

    public MessagingRuntimeMappingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
