package com.projeto_final.loan_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic loanRequestedTopic() {
        return TopicBuilder.name("loan-requested")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
