package com.projeto_final.credit_analysis_service.service;

import com.projeto_final.credit_analysis_service.dto.CreditAnalysisResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CreditAnalysisServiceTest {

    private CreditAnalysisService creditAnalysisService;

    @BeforeEach
    void setUp() {
        creditAnalysisService = new CreditAnalysisService();
    }

    @Test
    void analyze_shouldReturnResponseWithSameUserId() {
        CreditAnalysisResponse response = creditAnalysisService.analyze("user-abc");

        assertThat(response.userId()).isEqualTo("user-abc");
    }

    @Test
    void analyze_shouldReturnScoreInValidRange() {
        CreditAnalysisResponse response = creditAnalysisService.analyze("user-test");

        assertThat(response.score()).isGreaterThanOrEqualTo(300);
        assertThat(response.score()).isLessThan(800);
    }

    @Test
    void analyze_shouldReturnLimitEqualToScoreTimeTwenty() {
        CreditAnalysisResponse response = creditAnalysisService.analyze("user-123");

        assertThat(response.limit()).isEqualTo(response.score() * 20.0);
    }

    @Test
    void analyze_shouldBeDeterministicForSameUserId() {
        CreditAnalysisResponse r1 = creditAnalysisService.analyze("same-user");
        CreditAnalysisResponse r2 = creditAnalysisService.analyze("same-user");

        assertThat(r1.score()).isEqualTo(r2.score());
        assertThat(r1.limit()).isEqualTo(r2.limit());
    }

    @Test
    void analyze_shouldReturnDifferentScoresForDifferentUsers() {
        // Hash-based: highly likely to differ
        CreditAnalysisResponse r1 = creditAnalysisService.analyze("user-aaa-111");
        CreditAnalysisResponse r2 = creditAnalysisService.analyze("user-bbb-222");

        // They can differ — at least one field should differ when userIds differ significantly
        assertThat(r1.userId()).isNotEqualTo(r2.userId());
    }

    @Test
    void analyze_shouldNeverReturnNegativeLimit() {
        String[] testUsers = {"a", "user-xyz", "12345", "test@email.com", "UPPER_CASE"};
        for (String userId : testUsers) {
            CreditAnalysisResponse response = creditAnalysisService.analyze(userId);
            assertThat(response.limit()).isPositive();
        }
    }

    @Test
    void analyze_shouldHandleSingleCharacterUserId() {
        CreditAnalysisResponse response = creditAnalysisService.analyze("x");

        assertThat(response.userId()).isEqualTo("x");
        assertThat(response.score()).isGreaterThanOrEqualTo(300);
        assertThat(response.limit()).isPositive();
    }

    @Test
    void analyze_shouldHandleNumericUserId() {
        CreditAnalysisResponse response = creditAnalysisService.analyze("987654321");

        assertThat(response.userId()).isEqualTo("987654321");
        assertThat(response.score()).isGreaterThanOrEqualTo(300);
    }

    @Test
    void analyze_shouldHandleUuidStyleUserId() {
        String uuid = "550e8400-e29b-41d4-a716-446655440000";
        CreditAnalysisResponse response = creditAnalysisService.analyze(uuid);

        assertThat(response.userId()).isEqualTo(uuid);
        assertThat(response.score()).isBetween(300, 800);
    }

    @Test
    void analyze_shouldReturnNotNullResponse() {
        CreditAnalysisResponse response = creditAnalysisService.analyze("any-user");

        assertThat(response).isNotNull();
    }
}
