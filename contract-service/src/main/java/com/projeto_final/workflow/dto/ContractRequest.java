package com.projeto_final.workflow.dto;

public record ContractRequest(
        String loanId,
        String userId,
        double amount,
        int    installments
) {}
