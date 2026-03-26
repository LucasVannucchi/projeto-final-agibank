package com.projeto_final.workflow.service;

import com.projeto_final.workflow.dto.ContractResponse;
import com.projeto_final.workflow.model.Contract;
import com.projeto_final.workflow.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;

    public boolean existsByUserId(String userId) {
        return contractRepository.existsByUserId(userId);
    }

    public Optional<ContractResponse> findByUserId(String userId) {
        return contractRepository.findByUserId(userId).map(ContractResponse::from);
    }

    public ContractResponse createContract(String loanId, String userId, double amount, int installments) {
        Contract contract = Contract.builder()
                .contractId(UUID.randomUUID().toString())
                .loanId(loanId)
                .userId(userId)
                .amount(amount)
                .installments(installments)
                .createdAt(Instant.now())
                .build();

        Contract saved = contractRepository.save(contract);
        log.info("Contract created: contractId={} userId={}", saved.getContractId(), userId);
        return ContractResponse.from(saved);
    }
}
