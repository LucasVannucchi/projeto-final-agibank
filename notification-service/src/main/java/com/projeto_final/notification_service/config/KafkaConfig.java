package com.projeto_final.notification_service.config;

import com.projeto_final.notification_service.event.LoanApprovedEvent;
import com.projeto_final.notification_service.event.LoanRejectedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private <T> ConsumerFactory<String, T> consumerFactory(Class<T> type, String groupId) {
        JsonDeserializer<T> deser = new JsonDeserializer<>(type);
        deser.setRemoveTypeHeaders(false);
        deser.addTrustedPackages("*");
        deser.setUseTypeMapperForKey(false);
        return new DefaultKafkaConsumerFactory<>(
                Map.of(
                        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                        ConsumerConfig.GROUP_ID_CONFIG, groupId,
                        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
                ),
                new StringDeserializer(),
                deser
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, LoanApprovedEvent> approvedFactory() {
        var f = new ConcurrentKafkaListenerContainerFactory<String, LoanApprovedEvent>();
        f.setConsumerFactory(consumerFactory(LoanApprovedEvent.class, "notification-service-group"));
        return f;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, LoanRejectedEvent> rejectedFactory() {
        var f = new ConcurrentKafkaListenerContainerFactory<String, LoanRejectedEvent>();
        f.setConsumerFactory(consumerFactory(LoanRejectedEvent.class, "notification-service-group"));
        return f;
    }
}
