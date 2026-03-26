package com.projeto_final.loan_service.event;

import java.time.Instant;

public record LoanRequestedEvent(
        String loanId,
        String userId,
        String name,
        String email,
        double amount,
        int installments,
        int score,
        double limit,
        String timestamp
) {
    public static LoanRequestedEvent of(String loanId, String userId, String name,
                                        String email, double amount, int installments,
                                        int score, double limit) {
        return new LoanRequestedEvent(loanId, userId, name, email, amount,
                installments, score, limit, Instant.now().toString());
    }
}
