package com.projeto_final.workflow.dto;

import com.projeto_final.workflow.model.Contract;

public record ContractResponse(
        String contractId,
        String loanId,
        String userId,
        double amount,
        int    installments,
        String createdAt
) {
    public static ContractResponse from(Contract c) {
        return new ContractResponse(c.getContractId(), c.getLoanId(), c.getUserId(),
                c.getAmount(), c.getInstallments(), c.getCreatedAt().toString());
    }
}
