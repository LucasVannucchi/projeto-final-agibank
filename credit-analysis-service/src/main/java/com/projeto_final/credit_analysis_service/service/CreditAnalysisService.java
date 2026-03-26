package com.projeto_final.credit_analysis_service.service;

import com.projeto_final.credit_analysis_service.dto.CreditAnalysisResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CreditAnalysisService {

    public CreditAnalysisResponse analyze(String userId) {
        log.info("Running credit analysis for userId={}", userId);

        int hash  = Math.abs(userId.hashCode());
        int score = 300 + (hash % 500);
        double limit = score * 20.0;

        log.info("Credit analysis result: userId={} score={} limit={}", userId, score, limit);
        return new CreditAnalysisResponse(userId, score, limit);
    }
}
