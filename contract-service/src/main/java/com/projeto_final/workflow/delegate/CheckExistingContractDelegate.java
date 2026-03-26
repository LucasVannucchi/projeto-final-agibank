package com.projeto_final.workflow.delegate;

import com.projeto_final.workflow.service.ContractService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Slf4j
@Component("checkExistingContractDelegate")
@RequiredArgsConstructor
public class CheckExistingContractDelegate implements JavaDelegate {

    private final ContractService contractService;

    @Override
    public void execute(DelegateExecution execution) {
        String userId = (String) execution.getVariable("userId");
        log.info("[BPMN] CheckExistingContract — userId={}", userId);

        boolean exists = contractService.existsByUserId(userId);
        execution.setVariable("contractExists", exists);

        log.info("[BPMN] contractExists={} for userId={}", exists, userId);
    }
}
