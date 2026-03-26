package com.projeto_final.workflow.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.Instant;

@Data
@Builder
@Document(collection = "contracts")
public class Contract {

    @Id
    private String contractId;

    @Indexed
    private String loanId;

    @Indexed
    private String userId;

    private double  amount;
    private int     installments;
    private Instant createdAt;
}
