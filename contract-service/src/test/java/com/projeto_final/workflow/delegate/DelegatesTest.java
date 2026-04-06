package com.projeto_final.workflow.delegate;

import com.projeto_final.workflow.dto.ContractResponse;
import com.projeto_final.workflow.event.LoanApprovedEvent;
import com.projeto_final.workflow.event.LoanRejectedEvent;
import com.projeto_final.workflow.kafka.LoanEventProducer;
import com.projeto_final.workflow.service.ContractService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DelegatesTest {

    @Mock
    private DelegateExecution execution;

    // ---- CheckExistingContractDelegate ----

    @Mock
    private ContractService contractService;

    @Test
    void checkExistingContract_shouldSetContractExistsTrueWhenContractFound() throws Exception {
        CheckExistingContractDelegate delegate = new CheckExistingContractDelegate(contractService);
        when(execution.getVariable("userId")).thenReturn("user-1");
        when(contractService.existsByUserId("user-1")).thenReturn(true);

        delegate.execute(execution);

        verify(execution).setVariable("contractExists", true);
    }

    @Test
    void checkExistingContract_shouldSetContractExistsFalseWhenNoContract() throws Exception {
        CheckExistingContractDelegate delegate = new CheckExistingContractDelegate(contractService);
        when(execution.getVariable("userId")).thenReturn("user-new");
        when(contractService.existsByUserId("user-new")).thenReturn(false);

        delegate.execute(execution);

        verify(execution).setVariable("contractExists", false);
    }

    // ---- CheckScoreDelegate ----

    @Test
    void checkScore_shouldSetCategoryApprovedAutoWhenScoreAboveThreshold() throws Exception {
        CheckScoreDelegate delegate = new CheckScoreDelegate();
        when(execution.getVariable("score")).thenReturn(500);
        when(execution.getVariable("loanId")).thenReturn("loan-1");

        delegate.execute(execution);

        verify(execution).setVariable("scoreCategory", "APPROVED_AUTO");
    }

    @Test
    void checkScore_shouldSetCategoryApprovedAutoWhenScoreExactlyAtThreshold() throws Exception {
        CheckScoreDelegate delegate = new CheckScoreDelegate();
        when(execution.getVariable("score")).thenReturn(400);
        when(execution.getVariable("loanId")).thenReturn("loan-2");

        delegate.execute(execution);

        verify(execution).setVariable("scoreCategory", "APPROVED_AUTO");
    }

    @Test
    void checkScore_shouldSetCategoryRequiresManagerWhenScoreBelowThreshold() throws Exception {
        CheckScoreDelegate delegate = new CheckScoreDelegate();
        when(execution.getVariable("score")).thenReturn(399);
        when(execution.getVariable("loanId")).thenReturn("loan-3");

        delegate.execute(execution);

        verify(execution).setVariable("scoreCategory", "REQUIRES_MANAGER");
    }

    @Test
    void checkScore_shouldHandleMinimumScoreEdgeCase() throws Exception {
        CheckScoreDelegate delegate = new CheckScoreDelegate();
        when(execution.getVariable("score")).thenReturn(0);
        when(execution.getVariable("loanId")).thenReturn("loan-4");

        delegate.execute(execution);

        verify(execution).setVariable("scoreCategory", "REQUIRES_MANAGER");
    }

    // ---- CreateContractDelegate ----

    @Test
    void createContract_shouldCreateContractAndSetContractIdVariable() throws Exception {
        CreateContractDelegate delegate = new CreateContractDelegate(contractService);
        when(execution.getVariable("loanId")).thenReturn("loan-99");
        when(execution.getVariable("userId")).thenReturn("user-99");
        when(execution.getVariable("amount")).thenReturn(10000.0);
        when(execution.getVariable("installments")).thenReturn(24);

        ContractResponse response = new ContractResponse("cid-99", "loan-99", "user-99", 10000.0, 24, Instant.now().toString());
        when(contractService.createContract("loan-99", "user-99", 10000.0, 24)).thenReturn(response);

        delegate.execute(execution);

        verify(execution).setVariable("contractId", "cid-99");
    }

    @Test
    void createContract_shouldDelegateToServiceWithCorrectParams() throws Exception {
        CreateContractDelegate delegate = new CreateContractDelegate(contractService);
        when(execution.getVariable("loanId")).thenReturn("loan-A");
        when(execution.getVariable("userId")).thenReturn("user-A");
        when(execution.getVariable("amount")).thenReturn(2000.0);
        when(execution.getVariable("installments")).thenReturn(6);

        ContractResponse response = new ContractResponse("cid-A", "loan-A", "user-A", 2000.0, 6, Instant.now().toString());
        when(contractService.createContract(any(), any(), anyDouble(), anyInt())).thenReturn(response);

        delegate.execute(execution);

        verify(contractService).createContract("loan-A", "user-A", 2000.0, 6);
    }

    // ---- PublishApprovedDelegate ----

    @Mock
    private LoanEventProducer producer;

    @Test
    void publishApproved_shouldBuildAndPublishApprovedEvent() throws Exception {
        PublishApprovedDelegate delegate = new PublishApprovedDelegate(producer);
        when(execution.getVariable("loanId")).thenReturn("loan-ok");
        when(execution.getVariable("userId")).thenReturn("user-ok");
        when(execution.getVariable("name")).thenReturn("Alice");
        when(execution.getVariable("email")).thenReturn("alice@example.com");
        when(execution.getVariable("amount")).thenReturn(3000.0);
        when(execution.getVariable("installments")).thenReturn(18);
        when(execution.getVariable("contractId")).thenReturn("cid-ok");

        delegate.execute(execution);

        ArgumentCaptor<LoanApprovedEvent> captor = ArgumentCaptor.forClass(LoanApprovedEvent.class);
        verify(producer).publishApproved(captor.capture());

        LoanApprovedEvent event = captor.getValue();
        assertThat(event.loanId()).isEqualTo("loan-ok");
        assertThat(event.userId()).isEqualTo("user-ok");
        assertThat(event.name()).isEqualTo("Alice");
        assertThat(event.email()).isEqualTo("alice@example.com");
        assertThat(event.amount()).isEqualTo(3000.0);
        assertThat(event.installments()).isEqualTo(18);
        assertThat(event.contractId()).isEqualTo("cid-ok");
        assertThat(event.timestamp()).isNotBlank();
    }

    // ---- PublishRejectedDelegate ----

    @Test
    void publishRejected_shouldPublishWithExistingContractReasonWhenContractExists() throws Exception {
        PublishRejectedDelegate delegate = new PublishRejectedDelegate(producer);
        when(execution.getVariable("loanId")).thenReturn("loan-rej");
        when(execution.getVariable("userId")).thenReturn("user-rej");
        when(execution.getVariable("name")).thenReturn("Bob");
        when(execution.getVariable("email")).thenReturn("bob@example.com");
        when(execution.getVariable("contractExists")).thenReturn(true);

        delegate.execute(execution);

        ArgumentCaptor<LoanRejectedEvent> captor = ArgumentCaptor.forClass(LoanRejectedEvent.class);
        verify(producer).publishRejected(captor.capture());

        assertThat(captor.getValue().reason()).isEqualTo("User already has an active contract");
    }

    @Test
    void publishRejected_shouldPublishWithManagerDecisionReasonWhenContractDoesNotExist() throws Exception {
        PublishRejectedDelegate delegate = new PublishRejectedDelegate(producer);
        when(execution.getVariable("loanId")).thenReturn("loan-rej2");
        when(execution.getVariable("userId")).thenReturn("user-rej2");
        when(execution.getVariable("name")).thenReturn("Carol");
        when(execution.getVariable("email")).thenReturn("carol@example.com");
        when(execution.getVariable("contractExists")).thenReturn(false);

        delegate.execute(execution);

        ArgumentCaptor<LoanRejectedEvent> captor = ArgumentCaptor.forClass(LoanRejectedEvent.class);
        verify(producer).publishRejected(captor.capture());

        assertThat(captor.getValue().reason()).isEqualTo("Loan rejected by manager decision");
    }

    @Test
    void publishRejected_shouldPublishWithManagerDecisionReasonWhenContractExistsIsNull() throws Exception {
        PublishRejectedDelegate delegate = new PublishRejectedDelegate(producer);
        when(execution.getVariable("loanId")).thenReturn("loan-null");
        when(execution.getVariable("userId")).thenReturn("user-null");
        when(execution.getVariable("name")).thenReturn("Dan");
        when(execution.getVariable("email")).thenReturn("dan@example.com");
        when(execution.getVariable("contractExists")).thenReturn(null);

        delegate.execute(execution);

        ArgumentCaptor<LoanRejectedEvent> captor = ArgumentCaptor.forClass(LoanRejectedEvent.class);
        verify(producer).publishRejected(captor.capture());

        assertThat(captor.getValue().reason()).isEqualTo("Loan rejected by manager decision");
    }
}
