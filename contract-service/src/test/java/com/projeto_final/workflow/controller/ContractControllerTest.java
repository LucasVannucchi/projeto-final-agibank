package com.projeto_final.workflow.controller;

import com.projeto_final.workflow.dto.ContractResponse;
import com.projeto_final.workflow.service.ContractService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContractControllerTest {

    @Mock
    private ContractService contractService;

    @InjectMocks
    private ContractController contractController;

    private ContractResponse buildResponse() {
        return new ContractResponse("c-123", "l-456", "user-789", 5000.0, 12, Instant.now().toString());
    }

    @Test
    void shouldReturn200WithContractWhenUserExists() {
        when(contractService.findByUserId("user-789")).thenReturn(Optional.of(buildResponse()));

        ResponseEntity<ContractResponse> response = contractController.getByUserId("user-789");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().userId()).isEqualTo("user-789");
        verify(contractService).findByUserId("user-789");
    }

    @Test
    void shouldReturn404WhenUserHasNoContract() {
        when(contractService.findByUserId("user-000")).thenReturn(Optional.empty());

        ResponseEntity<ContractResponse> response = contractController.getByUserId("user-000");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void shouldReturnCorrectContractDetailsInBody() {
        ContractResponse expected = buildResponse();
        when(contractService.findByUserId("user-789")).thenReturn(Optional.of(expected));

        ResponseEntity<ContractResponse> response = contractController.getByUserId("user-789");

        ContractResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.contractId()).isEqualTo("c-123");
        assertThat(body.loanId()).isEqualTo("l-456");
        assertThat(body.amount()).isEqualTo(5000.0);
        assertThat(body.installments()).isEqualTo(12);
    }
}
