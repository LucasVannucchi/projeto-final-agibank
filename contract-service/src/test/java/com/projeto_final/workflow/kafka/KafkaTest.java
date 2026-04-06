package com.projeto_final.workflow.kafka;

import com.projeto_final.workflow.event.LoanApprovedEvent;
import com.projeto_final.workflow.event.LoanRejectedEvent;
import com.projeto_final.workflow.event.LoanRequestedEvent;
import org.camunda.bpm.engine.RuntimeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaTest {

    // ---- LoanEventProducer ----

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private LoanEventProducer loanEventProducer;

    @Test
    void publishApproved_shouldSendToLoanApprovedTopicWithCorrectKey() {
        LoanApprovedEvent event = new LoanApprovedEvent("loan-1", "user-1", "Alice", "a@b.com", 1000.0, 12, "c-1", "now");

        loanEventProducer.publishApproved(event);

        verify(kafkaTemplate).send("loan-approved", "loan-1", event);
    }

    @Test
    void publishRejected_shouldSendToLoanRejectedTopicWithCorrectKey() {
        LoanRejectedEvent event = new LoanRejectedEvent("loan-2", "user-2", "Bob", "b@c.com", "Low score", "now");

        loanEventProducer.publishRejected(event);

        verify(kafkaTemplate).send("loan-rejected", "loan-2", event);
    }

    // ---- LoanRequestedConsumer ----

    @Mock
    private RuntimeService runtimeService;

    @InjectMocks
    private LoanRequestedConsumer loanRequestedConsumer;

    @Test
    void consume_shouldStartCamundaProcessWithCorrectVariables() {
        LoanRequestedEvent event = new LoanRequestedEvent(
                "loan-X", "user-X", "Eve", "eve@mail.com", 5000.0, 24, 450, 10000.0, "2024-01-01T00:00:00Z");

        loanRequestedConsumer.consume(event);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(runtimeService).startProcessInstanceByKey(eq("loan-approval-process"), eq("loan-X"), captor.capture());

        Map<String, Object> vars = captor.getValue();
        assertThat(vars).containsEntry("loanId", "loan-X");
        assertThat(vars).containsEntry("userId", "user-X");
        assertThat(vars).containsEntry("name", "Eve");
        assertThat(vars).containsEntry("email", "eve@mail.com");
        assertThat(vars).containsEntry("amount", 5000.0);
        assertThat(vars).containsEntry("installments", 24);
        assertThat(vars).containsEntry("score", 450);
        assertThat(vars).containsEntry("limit", 10000.0);
    }

//    @Test
//    void consume_shouldUseEventLoanIdAsProcessBusinessKey() {
//        LoanRequestedEvent event = new LoanRequestedEvent(
//                "biz-key-99", "u1", "Test", "t@t.com", 100.0, 1, 500, 2000.0, "ts");
//
//        loanRequestedConsumer.consume(event);
//
//        verify(runtimeService).startProcessInstanceByKey(eq("loan-approval-process"), eq("biz-key-99"), any());
//    }
}
