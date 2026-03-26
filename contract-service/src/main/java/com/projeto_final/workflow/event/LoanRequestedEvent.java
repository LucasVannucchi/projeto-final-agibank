package com.projeto_final.workflow.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LoanRequestedEvent(
        String loanId,
        String userId,
        String name,
        String email,
        double amount,
        int    installments,
        int    score,
        double limit,
        String timestamp
) {}
