package com.vdobrikov.microservices.communication.rabbit.messaging.exception;

public class MessagingRuntimeException extends RuntimeException {
    public MessagingRuntimeException() {
    }

    public MessagingRuntimeException(String message) {
        super(message);
    }

    public MessagingRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessagingRuntimeException(Throwable cause) {
        super(cause);
    }

    public MessagingRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
