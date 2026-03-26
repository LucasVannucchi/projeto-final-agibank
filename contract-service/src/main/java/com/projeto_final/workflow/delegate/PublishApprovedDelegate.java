package com.projeto_final.workflow.delegate;

import com.projeto_final.workflow.event.LoanApprovedEvent;
import com.projeto_final.workflow.kafka.LoanEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component("publishApprovedDelegate")
@RequiredArgsConstructor
public class PublishApprovedDelegate implements JavaDelegate {

    private final LoanEventProducer producer;

    @Override
    public void execute(DelegateExecution execution) {
        String loanId       = (String) execution.getVariable("loanId");
        String userId       = (String) execution.getVariable("userId");
        String name         = (String) execution.getVariable("name");
        String email        = (String) execution.getVariable("email");
        double amount       = ((Number) execution.getVariable("amount")).doubleValue();
        int    installments = ((Number) execution.getVariable("installments")).intValue();
        String contractId   = (String) execution.getVariable("contractId");

        log.info("[BPMN] PublishApproved — loanId={}", loanId);

        LoanApprovedEvent event = new LoanApprovedEvent(
                loanId, userId, name, email, amount, installments,
                contractId, Instant.now().toString());

        producer.publishApproved(event);
    }
}
