package com.projeto_final.workflow.event;

import java.time.Instant;

public record LoanApprovedEvent(
        String loanId,
        String userId,
        String name,
        String email,
        double amount,
        int    installments,
        String contractId,
        String timestamp
) {
    public static LoanApprovedEvent from(LoanRequestedEvent req, String contractId) {
        return new LoanApprovedEvent(req.loanId(), req.userId(), req.name(), req.email(),
                req.amount(), req.installments(), contractId, Instant.now().toString());
    }
}
