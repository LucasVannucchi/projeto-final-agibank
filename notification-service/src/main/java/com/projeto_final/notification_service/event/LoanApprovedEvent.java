package com.projeto_final.notification_service.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LoanApprovedEvent(
        String loanId,
        String userId,
        String name,
        String email,
        double amount,
        int    installments,
        String contractId,
        String timestamp
) {}
