package com.projeto_final.credit_analysis_service.controller;

import com.projeto_final.credit_analysis_service.dto.CreditAnalysisResponse;
import com.projeto_final.credit_analysis_service.service.CreditAnalysisService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreditAnalysisControllerTest {

    @Mock
    private CreditAnalysisService creditAnalysisService;

    @InjectMocks
    private CreditAnalysisController creditAnalysisController;

    @Test
    void analyze_shouldDelegateToServiceAndReturnResponse() {
        CreditAnalysisResponse expected = new CreditAnalysisResponse("user-1", 600, 12000.0);
        when(creditAnalysisService.analyze("user-1")).thenReturn(expected);

        CreditAnalysisResponse result = creditAnalysisController.analyze("user-1");

        assertThat(result).isEqualTo(expected);
        verify(creditAnalysisService).analyze("user-1");
    }

    @Test
    void analyze_shouldReturnCorrectUserIdInResponse() {
        when(creditAnalysisService.analyze("user-xyz"))
                .thenReturn(new CreditAnalysisResponse("user-xyz", 400, 8000.0));

        CreditAnalysisResponse result = creditAnalysisController.analyze("user-xyz");

        assertThat(result.userId()).isEqualTo("user-xyz");
    }

    @Test
    void analyze_shouldReturnCorrectScoreAndLimit() {
        when(creditAnalysisService.analyze("user-2"))
                .thenReturn(new CreditAnalysisResponse("user-2", 750, 15000.0));

        CreditAnalysisResponse result = creditAnalysisController.analyze("user-2");

        assertThat(result.score()).isEqualTo(750);
        assertThat(result.limit()).isEqualTo(15000.0);
    }

    @Test
    void analyze_shouldCallServiceWithExactPathVariableValue() {
        String userId = "specific-user-id";
        when(creditAnalysisService.analyze(userId))
                .thenReturn(new CreditAnalysisResponse(userId, 500, 10000.0));

        creditAnalysisController.analyze(userId);

        verify(creditAnalysisService).analyze(userId);
    }
}
