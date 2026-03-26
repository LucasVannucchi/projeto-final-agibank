package com.projeto_final.loan_service.client;

import com.projeto_final.loan_service.dto.CreditAnalysisResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CreditAnalysisClient {

    private final RestClient restClient;

    public CreditAnalysisClient(@Value("${credit-analysis.url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public CreditAnalysisResponse analyze(String userId) {
        return restClient.get()
                .uri("/analysis/{userId}", userId)
                .retrieve()
                .body(CreditAnalysisResponse.class);
    }
}
