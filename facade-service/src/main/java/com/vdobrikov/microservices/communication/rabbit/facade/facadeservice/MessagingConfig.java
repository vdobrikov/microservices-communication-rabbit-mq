package com.vdobrikov.microservices.communication.rabbit.facade.facadeservice;

import com.vdobrikov.microservices.communication.rabbit.messaging.Publisher;
import com.vdobrikov.microservices.communication.rabbit.messaging.rabbit.RabbitMQPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfig {

    @Bean
    public Publisher publisher(@Value("${rabbitmq.host}") String hostname) {
        return new RabbitMQPublisher(hostname);
    }
}
