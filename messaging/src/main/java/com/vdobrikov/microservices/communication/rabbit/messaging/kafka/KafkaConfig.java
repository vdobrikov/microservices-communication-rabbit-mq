package com.vdobrikov.microservices.communication.rabbit.messaging.kafka;

import com.vdobrikov.microservices.communication.rabbit.messaging.Config;

public class KafkaConfig extends Config {
    public static final String KAFKA_FAILED_TOPIC = "kafka.failed.topic";
    public static final String KAFKA_FAILED_TOPIC_VALUE = "failed_messages";
}
