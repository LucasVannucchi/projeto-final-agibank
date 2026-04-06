package com.projeto_final.loan_service.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    // --- InsufficientLimitException ---

    @Test
    void handleInsufficientLimit_shouldReturnMapWithErrorAndCode() {
        InsufficientLimitException ex = new InsufficientLimitException("Amount 5000 exceeds limit 2000");

        Map<String, String> result = handler.handleInsufficientLimit(ex);

        assertThat(result).containsEntry("error", "Amount 5000 exceeds limit 2000");
        assertThat(result).containsEntry("code", "INSUFFICIENT_LIMIT");
    }

    @Test
    void handleInsufficientLimit_shouldPreserveOriginalMessage() {
        String msg = "Requested amount 9999.00 exceeds credit limit 5000.00";
        Map<String, String> result = handler.handleInsufficientLimit(new InsufficientLimitException(msg));

        assertThat(result.get("error")).isEqualTo(msg);
    }

    // --- MethodArgumentNotValidException ---

    @Test
    void handleValidation_shouldReturnFieldErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError = new FieldError("loanRequest", "amount", "amount must be positive");
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        Map<String, String> result = handler.handleValidation(ex);

        assertThat(result).containsEntry("amount", "amount must be positive");
    }

    @Test
    void handleValidation_shouldReturnAllFieldErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        List<org.springframework.validation.ObjectError> errors = List.of(
                new FieldError("req", "userId", "userId is required"),
                new FieldError("req", "email", "email is required")
        );
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(errors);

        Map<String, String> result = handler.handleValidation(ex);

        assertThat(result).hasSize(2);
        assertThat(result).containsEntry("userId", "userId is required");
        assertThat(result).containsEntry("email", "email is required");
    }

    @Test
    void handleValidation_shouldReturnEmptyMapWhenNoErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of());

        Map<String, String> result = handler.handleValidation(ex);

        assertThat(result).isEmpty();
    }

    // --- Generic Exception ---

    @Test
    void handleGeneric_shouldReturnInternalErrorWithMessage() {
        Exception ex = new RuntimeException("Unexpected failure");

        Map<String, String> result = handler.handleGeneric(ex);

        assertThat(result).containsEntry("error", "Unexpected failure");
        assertThat(result).containsEntry("code", "INTERNAL_ERROR");
    }

    // --- InsufficientLimitException domain ---

    @Test
    void insufficientLimitException_shouldExtendRuntimeException() {
        InsufficientLimitException ex = new InsufficientLimitException("limit error");

        assertThat(ex).isInstanceOf(RuntimeException.class);
        assertThat(ex.getMessage()).isEqualTo("limit error");
    }
}
