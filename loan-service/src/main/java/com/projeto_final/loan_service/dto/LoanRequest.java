package com.projeto_final.loan_service.dto;

import jakarta.validation.constraints.*;

public record LoanRequest(
        @NotBlank(message = "userId is required")       String userId,
        @NotBlank(message = "name is required")         String name,
        @Email @NotBlank(message = "email is required") String email,
        @Positive(message = "amount must be positive")  double amount,
        @Min(1) @Max(360)                               int installments
) {}
