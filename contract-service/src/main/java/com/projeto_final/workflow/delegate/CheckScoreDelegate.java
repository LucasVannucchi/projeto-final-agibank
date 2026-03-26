package com.projeto_final.workflow.delegate;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Slf4j
@Component("checkScoreDelegate")
public class CheckScoreDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        int score = ((Number) execution.getVariable("score")).intValue();
        String loanId = (String) execution.getVariable("loanId");

        String category = score >= 400 ? "APPROVED_AUTO" : "REQUIRES_MANAGER";
        execution.setVariable("scoreCategory", category);

        log.info("[BPMN] CheckScore — loanId={} score={} category={}", loanId, score, category);
    }
}
