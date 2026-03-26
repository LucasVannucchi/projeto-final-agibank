package com.projeto_final.workflow.delegate;

import com.projeto_final.workflow.dto.ContractResponse;
import com.projeto_final.workflow.service.ContractService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Slf4j
@Component("createContractDelegate")
@RequiredArgsConstructor
public class CreateContractDelegate implements JavaDelegate {

    private final ContractService contractService;

    @Override
    public void execute(DelegateExecution execution) {
        String loanId       = (String) execution.getVariable("loanId");
        String userId       = (String) execution.getVariable("userId");
        double amount       = ((Number) execution.getVariable("amount")).doubleValue();
        int    installments = ((Number) execution.getVariable("installments")).intValue();

        log.info("[BPMN] CreateContract — loanId={} userId={}", loanId, userId);

        ContractResponse contract = contractService.createContract(loanId, userId, amount, installments);
        execution.setVariable("contractId", contract.contractId());

        log.info("[BPMN] Contract created: contractId={}", contract.contractId());
    }
}
