package com.projeto_final.workflow.delegate;

import com.projeto_final.workflow.event.LoanRejectedEvent;
import com.projeto_final.workflow.kafka.LoanEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component("publishRejectedDelegate")
@RequiredArgsConstructor
public class PublishRejectedDelegate implements JavaDelegate {

    private final LoanEventProducer producer;

    @Override
    public void execute(DelegateExecution execution) {
        String loanId = (String) execution.getVariable("loanId");
        String userId = (String) execution.getVariable("userId");
        String name = (String) execution.getVariable("name");
        String email = (String) execution.getVariable("email");

        Boolean contractExists = (Boolean) execution.getVariable("contractExists");
        String reason = (contractExists != null && contractExists)
                ? "User already has an active contract"
                : "Loan rejected by manager decision";

        log.info("[BPMN] PublishRejected — loanId={} reason={}", loanId, reason);

        LoanRejectedEvent event = new LoanRejectedEvent(
                loanId, userId, name, email, reason, Instant.now().toString());

        producer.publishRejected(event);
    }
}
