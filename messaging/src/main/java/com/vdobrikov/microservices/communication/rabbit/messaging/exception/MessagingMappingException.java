package com.vdobrikov.microservices.communication.rabbit.messaging.exception;

public class MessagingMappingException extends MessagingException {
    public MessagingMappingException() {
    }

    public MessagingMappingException(String message) {
        super(message);
    }

    public MessagingMappingException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessagingMappingException(Throwable cause) {
        super(cause);
    }

    public MessagingMappingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
