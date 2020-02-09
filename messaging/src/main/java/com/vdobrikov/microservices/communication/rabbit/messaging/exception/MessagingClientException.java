package com.vdobrikov.microservices.communication.rabbit.messaging.exception;

public class MessagingClientException extends MessagingException {
    public MessagingClientException() {
    }

    public MessagingClientException(String message) {
        super(message);
    }

    public MessagingClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessagingClientException(Throwable cause) {
        super(cause);
    }

    public MessagingClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
