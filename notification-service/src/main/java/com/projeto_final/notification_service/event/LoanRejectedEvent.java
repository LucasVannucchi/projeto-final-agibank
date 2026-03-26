package com.projeto_final.notification_service.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LoanRejectedEvent(
        String loanId,
        String userId,
        String name,
        String email,
        double amount,
        String reason,
        String timestamp
) {}
