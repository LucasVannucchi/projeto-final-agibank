package com.projeto_final.workflow.event;

import java.time.Instant;

public record LoanRejectedEvent(
        String loanId,
        String userId,
        String name,
        String email,
        String reason,
        String timestamp
) {
    public static LoanRejectedEvent from(LoanRequestedEvent req, String reason) {
        return new LoanRejectedEvent(req.loanId(), req.userId(), req.name(), req.email(),
                reason, Instant.now().toString());
    }
}
