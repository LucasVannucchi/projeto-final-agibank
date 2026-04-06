package com.projeto_final.loan_service.service;

import com.projeto_final.loan_service.client.CreditAnalysisClient;
import com.projeto_final.loan_service.dto.CreditAnalysisResponse;
import com.projeto_final.loan_service.dto.LoanRequest;
import com.projeto_final.loan_service.dto.LoanResponse;
import com.projeto_final.loan_service.event.LoanRequestedEvent;
import com.projeto_final.loan_service.exception.InsufficientLimitException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;
    @Mock private CreditAnalysisClient creditAnalysisClient;
    @Mock private KafkaTemplate<String, LoanRequestedEvent> kafkaTemplate;

    @InjectMocks
    private LoanService loanService;

    private LoanRequest defaultRequest;

    @BeforeEach
    void setUp() {
        defaultRequest = new LoanRequest("user-1", "Alice", "alice@mail.com", 3000.0, 12);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    // --- Cache miss path ---

    @Test
    void requestLoan_shouldCallCreditAnalysisClientWhenCacheMiss() {
        when(valueOperations.get("user:user-1:limit")).thenReturn(null);
        when(valueOperations.get("user:user-1:score")).thenReturn(null);
        when(creditAnalysisClient.analyze("user-1"))
                .thenReturn(new CreditAnalysisResponse("user-1", 600, 12000.0));

        loanService.requestLoan(defaultRequest);

        verify(creditAnalysisClient).analyze("user-1");
    }

    @Test
    void requestLoan_shouldCacheScoreAndLimitAfterCreditAnalysisOnCacheMiss() {
        when(valueOperations.get(anyString())).thenReturn(null);
        when(creditAnalysisClient.analyze("user-1"))
                .thenReturn(new CreditAnalysisResponse("user-1", 600, 12000.0));

        loanService.requestLoan(defaultRequest);

        verify(valueOperations).set(eq("user:user-1:limit"), eq("12000.0"), any(Duration.class));
        verify(valueOperations).set(eq("user:user-1:score"), eq("600"), any(Duration.class));
    }

    @Test
    void requestLoan_shouldPublishKafkaEventAfterSuccessfulValidation() {
        when(valueOperations.get(anyString())).thenReturn(null);
        when(creditAnalysisClient.analyze("user-1"))
                .thenReturn(new CreditAnalysisResponse("user-1", 600, 12000.0));

        loanService.requestLoan(defaultRequest);

        verify(kafkaTemplate).send(eq("loan-requested"), anyString(), any(LoanRequestedEvent.class));
    }

    @Test
    void requestLoan_shouldReturnAcceptedResponseWithLoanId() {
        when(valueOperations.get(anyString())).thenReturn(null);
        when(creditAnalysisClient.analyze("user-1"))
                .thenReturn(new CreditAnalysisResponse("user-1", 600, 12000.0));

        LoanResponse response = loanService.requestLoan(defaultRequest);

        assertThat(response).isNotNull();
        assertThat(response.loanId()).isNotBlank();
        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(response.message()).isNotBlank();
    }

    // --- Cache hit path ---

    @Test
    void requestLoan_shouldNotCallCreditAnalysisWhenCacheHit() {
        when(valueOperations.get("user:user-1:limit")).thenReturn("12000.0");
        when(valueOperations.get("user:user-1:score")).thenReturn("600");

        loanService.requestLoan(defaultRequest);

        verifyNoInteractions(creditAnalysisClient);
    }

    @Test
    void requestLoan_shouldUseCachedValuesAndPublishEventOnCacheHit() {
        when(valueOperations.get("user:user-1:limit")).thenReturn("12000.0");
        when(valueOperations.get("user:user-1:score")).thenReturn("600");

        LoanResponse response = loanService.requestLoan(defaultRequest);

        assertThat(response.status()).isEqualTo("PENDING");
        verify(kafkaTemplate).send(eq("loan-requested"), anyString(), any(LoanRequestedEvent.class));
    }

    // --- InsufficientLimitException ---

    @Test
    void requestLoan_shouldThrowInsufficientLimitExceptionWhenAmountExceedsLimit() {
        when(valueOperations.get("user:user-1:limit")).thenReturn("2000.0");
        when(valueOperations.get("user:user-1:score")).thenReturn("400");

        // Request amount (3000) > limit (2000)
        assertThatThrownBy(() -> loanService.requestLoan(defaultRequest))
                .isInstanceOf(InsufficientLimitException.class)
                .hasMessageContaining("3000")
                .hasMessageContaining("2000");
    }

    @Test
    void requestLoan_shouldNotPublishKafkaEventWhenLimitInsufficient() {
        when(valueOperations.get("user:user-1:limit")).thenReturn("100.0");
        when(valueOperations.get("user:user-1:score")).thenReturn("350");

        assertThatThrownBy(() -> loanService.requestLoan(defaultRequest))
                .isInstanceOf(InsufficientLimitException.class);

        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void requestLoan_shouldNotThrowWhenAmountExactlyEqualsLimit() {
        LoanRequest exactRequest = new LoanRequest("user-1", "Alice", "alice@mail.com", 12000.0, 12);
        when(valueOperations.get("user:user-1:limit")).thenReturn("12000.0");
        when(valueOperations.get("user:user-1:score")).thenReturn("600");

        LoanResponse response = loanService.requestLoan(exactRequest);

        assertThat(response.status()).isEqualTo("PENDING");
    }

    // --- Kafka event payload ---

    @Test
    void requestLoan_shouldPublishEventWithCorrectFieldsFromRequest() {
        when(valueOperations.get("user:user-1:limit")).thenReturn("12000.0");
        when(valueOperations.get("user:user-1:score")).thenReturn("550");

        loanService.requestLoan(defaultRequest);

        ArgumentCaptor<LoanRequestedEvent> captor = ArgumentCaptor.forClass(LoanRequestedEvent.class);
        verify(kafkaTemplate).send(eq("loan-requested"), anyString(), captor.capture());

        LoanRequestedEvent event = captor.getValue();
        assertThat(event.userId()).isEqualTo("user-1");
        assertThat(event.name()).isEqualTo("Alice");
        assertThat(event.email()).isEqualTo("alice@mail.com");
        assertThat(event.amount()).isEqualTo(3000.0);
        assertThat(event.installments()).isEqualTo(12);
        assertThat(event.score()).isEqualTo(550);
        assertThat(event.limit()).isEqualTo(12000.0);
        assertThat(event.loanId()).isNotBlank();
        assertThat(event.timestamp()).isNotBlank();
    }

    @Test
    void requestLoan_shouldUseEventLoanIdAsKafkaMessageKey() {
        when(valueOperations.get("user:user-1:limit")).thenReturn("12000.0");
        when(valueOperations.get("user:user-1:score")).thenReturn("550");

        LoanResponse response = loanService.requestLoan(defaultRequest);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<LoanRequestedEvent> eventCaptor = ArgumentCaptor.forClass(LoanRequestedEvent.class);
        verify(kafkaTemplate).send(eq("loan-requested"), keyCaptor.capture(), eventCaptor.capture());

        assertThat(keyCaptor.getValue()).isEqualTo(response.loanId());
        assertThat(eventCaptor.getValue().loanId()).isEqualTo(response.loanId());
    }

    // --- Only one cache key missing ---

    @Test
    void requestLoan_shouldCallCreditAnalysisWhenOnlyLimitCacheIsMissing() {
        when(valueOperations.get("user:user-1:limit")).thenReturn(null);
        when(valueOperations.get("user:user-1:score")).thenReturn(null);
        when(creditAnalysisClient.analyze("user-1"))
                .thenReturn(new CreditAnalysisResponse("user-1", 600, 12000.0));

        loanService.requestLoan(defaultRequest);

        verify(creditAnalysisClient).analyze("user-1");
    }
}
