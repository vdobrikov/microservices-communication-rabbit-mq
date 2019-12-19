package com.vdobrikov.microservices.communication.rabbit.facade.facadeservice;

import com.vdobrikov.microservices.communication.rabbit.messaging.Config;
import com.vdobrikov.microservices.communication.rabbit.messaging.Publisher;
import com.vdobrikov.microservices.communication.rabbit.messaging.kafka.KafkaPublisher;
import com.vdobrikov.microservices.communication.rabbit.messaging.rabbit.RabbitMQPublisher;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.vdobrikov.microservices.communication.rabbit.messaging.rabbit.RabbitMQConfig.RABBITMQ_HOST;

@Configuration
public class MessagingConfig {

    @Bean
    public Publisher publisher(@Value("${KAFKA_SERVERS:localhost:9092}") String hostname) {
//        return new RabbitMQPublisher(Config.build().with(RABBITMQ_HOST, hostname));
        return new KafkaPublisher(Config.build().with(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, hostname));
    }
}
