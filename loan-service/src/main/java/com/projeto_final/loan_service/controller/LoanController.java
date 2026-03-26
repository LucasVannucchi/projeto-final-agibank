package com.projeto_final.loan_service.controller;

import com.projeto_final.loan_service.dto.LoanRequest;
import com.projeto_final.loan_service.dto.LoanResponse;
import com.projeto_final.loan_service.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    /**
     * POST /loans
     * Accepts a loan request, validates credit limit via Redis/CreditAnalysis,
     * and publishes a loan-requested Kafka event.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public LoanResponse requestLoan(@RequestBody @Valid LoanRequest request) {
        return loanService.requestLoan(request);
    }
}
