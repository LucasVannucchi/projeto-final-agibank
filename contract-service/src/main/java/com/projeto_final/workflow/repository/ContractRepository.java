package com.projeto_final.workflow.repository;

import com.projeto_final.workflow.model.Contract;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContractRepository extends MongoRepository<Contract, String> {
    Optional<Contract> findByUserId(String userId);
    boolean existsByUserId(String userId);
}
