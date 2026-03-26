package com.projeto_final.credit_analysis_service.controller;

import com.projeto_final.credit_analysis_service.dto.CreditAnalysisResponse;
import com.projeto_final.credit_analysis_service.service.CreditAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analysis")
@RequiredArgsConstructor
public class CreditAnalysisController {

    private final CreditAnalysisService creditAnalysisService;

    @GetMapping("/{userId}")
    public CreditAnalysisResponse analyze(@PathVariable String userId) {
        return creditAnalysisService.analyze(userId);
    }
}
