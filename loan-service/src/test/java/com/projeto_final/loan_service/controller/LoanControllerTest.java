package com.projeto_final.loan_service.controller;

import com.projeto_final.loan_service.dto.LoanRequest;
import com.projeto_final.loan_service.dto.LoanResponse;
import com.projeto_final.loan_service.service.LoanService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanControllerTest {

    @Mock
    private LoanService loanService;

    @InjectMocks
    private LoanController loanController;

    @Test
    void requestLoan_shouldDelegateToServiceAndReturnResponse() {
        LoanRequest request = new LoanRequest("u1", "Alice", "a@b.com", 1000.0, 12);
        LoanResponse expected = LoanResponse.accepted("loan-abc");
        when(loanService.requestLoan(request)).thenReturn(expected);

        LoanResponse result = loanController.requestLoan(request);

        assertThat(result).isEqualTo(expected);
        verify(loanService).requestLoan(request);
    }

    @Test
    void requestLoan_shouldReturnPendingStatus() {
        LoanRequest request = new LoanRequest("u2", "Bob", "b@b.com", 2000.0, 6);
        when(loanService.requestLoan(request)).thenReturn(LoanResponse.accepted("loan-xyz"));

        LoanResponse result = loanController.requestLoan(request);

        assertThat(result.status()).isEqualTo("PENDING");
        assertThat(result.loanId()).isEqualTo("loan-xyz");
    }

    @Test
    void requestLoan_shouldPropagateExceptionFromService() {
        LoanRequest request = new LoanRequest("u3", "Carol", "c@b.com", 99999.0, 6);
        when(loanService.requestLoan(request))
                .thenThrow(new RuntimeException("Insufficient limit"));

        assertThatThrownBy(() -> loanController.requestLoan(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Insufficient limit");
    }
}
