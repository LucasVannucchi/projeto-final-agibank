package com.projeto_final.workflow.kafka;

import com.projeto_final.workflow.event.LoanApprovedEvent;
import com.projeto_final.workflow.event.LoanRejectedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoanEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishApproved(LoanApprovedEvent event) {
        kafkaTemplate.send("loan-approved", event.loanId(), event);
        log.info("Published loan-approved: loanId={}", event.loanId());
    }

    public void publishRejected(LoanRejectedEvent event) {
        kafkaTemplate.send("loan-rejected", event.loanId(), event);
        log.info("Published loan-rejected: loanId={}", event.loanId());
    }
}
