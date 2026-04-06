package com.projeto_final.workflow.service;

import com.projeto_final.workflow.dto.ContractResponse;
import com.projeto_final.workflow.model.Contract;
import com.projeto_final.workflow.repository.ContractRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractServiceTest {

    @Mock
    private ContractRepository contractRepository;

    @InjectMocks
    private ContractService contractService;

    private Contract sampleContract;

    @BeforeEach
    void setUp() {
        sampleContract = Contract.builder()
                .contractId("contract-123")
                .loanId("loan-456")
                .userId("user-789")
                .amount(5000.0)
                .installments(12)
                .createdAt(Instant.now())
                .build();
    }

    // --- existsByUserId ---

    @Test
    void shouldReturnTrueWhenContractExistsForUser() {
        when(contractRepository.existsByUserId("user-789")).thenReturn(true);

        boolean result = contractService.existsByUserId("user-789");

        assertThat(result).isTrue();
        verify(contractRepository).existsByUserId("user-789");
    }

    @Test
    void shouldReturnFalseWhenContractDoesNotExistForUser() {
        when(contractRepository.existsByUserId("user-000")).thenReturn(false);

        boolean result = contractService.existsByUserId("user-000");

        assertThat(result).isFalse();
    }

    // --- findByUserId ---

    @Test
    void shouldReturnContractResponseWhenUserExists() {
        when(contractRepository.findByUserId("user-789")).thenReturn(Optional.of(sampleContract));

        Optional<ContractResponse> result = contractService.findByUserId("user-789");

        assertThat(result).isPresent();
        assertThat(result.get().contractId()).isEqualTo("contract-123");
        assertThat(result.get().loanId()).isEqualTo("loan-456");
        assertThat(result.get().userId()).isEqualTo("user-789");
        assertThat(result.get().amount()).isEqualTo(5000.0);
        assertThat(result.get().installments()).isEqualTo(12);
    }

    @Test
    void shouldReturnEmptyWhenUserHasNoContract() {
        when(contractRepository.findByUserId("user-000")).thenReturn(Optional.empty());

        Optional<ContractResponse> result = contractService.findByUserId("user-000");

        assertThat(result).isEmpty();
    }

    // --- createContract ---

    @Test
    void shouldCreateContractAndReturnResponseWithGeneratedId() {
        when(contractRepository.save(any(Contract.class))).thenReturn(sampleContract);

        ContractResponse response = contractService.createContract("loan-456", "user-789", 5000.0, 12);

        assertThat(response).isNotNull();
        assertThat(response.contractId()).isEqualTo("contract-123");
        assertThat(response.loanId()).isEqualTo("loan-456");
        assertThat(response.userId()).isEqualTo("user-789");
        assertThat(response.amount()).isEqualTo(5000.0);
        assertThat(response.installments()).isEqualTo(12);
    }

    @Test
    void shouldSaveContractWithCorrectFieldsWhenCreating() {
        when(contractRepository.save(any(Contract.class))).thenReturn(sampleContract);

        ArgumentCaptor<Contract> captor = ArgumentCaptor.forClass(Contract.class);
        contractService.createContract("loan-456", "user-789", 5000.0, 12);

        verify(contractRepository).save(captor.capture());
        Contract saved = captor.getValue();

        assertThat(saved.getLoanId()).isEqualTo("loan-456");
        assertThat(saved.getUserId()).isEqualTo("user-789");
        assertThat(saved.getAmount()).isEqualTo(5000.0);
        assertThat(saved.getInstallments()).isEqualTo(12);
        assertThat(saved.getContractId()).isNotBlank();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldGenerateUniqueContractIdForEachCreation() {
        Contract contract1 = Contract.builder().contractId("id-1").loanId("l1").userId("u1")
                .amount(1000).installments(6).createdAt(Instant.now()).build();
        Contract contract2 = Contract.builder().contractId("id-2").loanId("l2").userId("u2")
                .amount(2000).installments(12).createdAt(Instant.now()).build();

        when(contractRepository.save(any())).thenReturn(contract1).thenReturn(contract2);

        ContractResponse r1 = contractService.createContract("l1", "u1", 1000, 6);
        ContractResponse r2 = contractService.createContract("l2", "u2", 2000, 12);

        assertThat(r1.contractId()).isNotEqualTo(r2.contractId());
    }

    @Test
    void shouldCreateContractWithZeroInstallmentsEdgeCase() {
        Contract contract = Contract.builder().contractId("c-0").loanId("l0").userId("u0")
                .amount(100).installments(0).createdAt(Instant.now()).build();
        when(contractRepository.save(any())).thenReturn(contract);

        ContractResponse response = contractService.createContract("l0", "u0", 100.0, 0);

        assertThat(response.installments()).isEqualTo(0);
    }
}
