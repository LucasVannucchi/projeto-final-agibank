package com.projeto_final.workflow.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic loanApprovedTopic() {
        return TopicBuilder.name("loan-approved").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic loanRejectedTopic() {
        return TopicBuilder.name("loan-rejected").partitions(3).replicas(1).build();
    }
}
