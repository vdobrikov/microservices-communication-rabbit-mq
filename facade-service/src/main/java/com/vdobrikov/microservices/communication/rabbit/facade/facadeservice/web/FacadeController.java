package com.vdobrikov.microservices.communication.rabbit.facade.facadeservice.web;

import com.vdobrikov.microservices.communication.rabbit.messaging.Publisher;
import com.vdobrikov.microservices.communication.rabbit.messaging.Wrapper;
import com.vdobrikov.microservices.communication.rabbit.messaging.exception.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@Slf4j
public class FacadeController {

    private final Publisher publisher;

    public FacadeController(Publisher publisher) {
        this.publisher = publisher;
    }

    @PostMapping("/publish")
    public Message publish(@RequestBody @Valid Message message) throws MessagingException {
        log.info("message={}", message);

        publisher.publish(message.getTopic(), new Wrapper()
                .withForwardTopic(message.getForwardToTopic())
                .withObject(message.getMessage()));
        log.info("Published");

        return message;
    }
}

