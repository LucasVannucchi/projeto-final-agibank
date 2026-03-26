package com.projeto_final.workflow.kafka;

import com.projeto_final.workflow.event.LoanRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoanRequestedConsumer {

    private final RuntimeService runtimeService;

    @KafkaListener(topics = "loan-requested", groupId = "contract-service-group")
    public void consume(LoanRequestedEvent event) {
        log.info("Received loan-requested event: loanId={} userId={}", event.loanId(), event.userId());

        Map<String, Object> variables = Map.of(
                "loanId",       event.loanId(),
                "userId",       event.userId(),
                "name",         event.name(),
                "email",        event.email(),
                "amount",       event.amount(),
                "installments", event.installments(),
                "score",        event.score(),
                "limit",        event.limit(),
                "timestamp",    event.timestamp()
        );

        runtimeService.startProcessInstanceByKey("loan-approval-process", event.loanId(), variables);
        log.info("Camunda process started for loanId={}", event.loanId());
    }
}
