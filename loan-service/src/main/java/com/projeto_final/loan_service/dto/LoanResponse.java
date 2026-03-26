package com.projeto_final.loan_service.dto;

public record LoanResponse(String loanId, String status, String message) {
    public static LoanResponse accepted(String loanId) {
        return new LoanResponse(loanId, "PENDING", "Loan request accepted and is being processed.");
    }
}
