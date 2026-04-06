package com.projeto_final.loan_service.event;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoanRequestedEventTest {

    @Test
    void of_shouldPopulateAllFields() {
        LoanRequestedEvent event = LoanRequestedEvent.of(
                "loan-1", "user-1", "Alice", "alice@mail.com", 3000.0, 12, 600, 12000.0);

        assertThat(event.loanId()).isEqualTo("loan-1");
        assertThat(event.userId()).isEqualTo("user-1");
        assertThat(event.name()).isEqualTo("Alice");
        assertThat(event.email()).isEqualTo("alice@mail.com");
        assertThat(event.amount()).isEqualTo(3000.0);
        assertThat(event.installments()).isEqualTo(12);
        assertThat(event.score()).isEqualTo(600);
        assertThat(event.limit()).isEqualTo(12000.0);
    }

    @Test
    void of_shouldGenerateNonNullTimestamp() {
        LoanRequestedEvent event = LoanRequestedEvent.of(
                "l", "u", "N", "e@m.com", 100.0, 1, 400, 5000.0);

        assertThat(event.timestamp()).isNotBlank();
    }

    @Test
    void of_shouldGenerateIso8601Timestamp() {
        LoanRequestedEvent event = LoanRequestedEvent.of(
                "l", "u", "N", "e@m.com", 100.0, 1, 400, 5000.0);

        // Instant.now().toString() produces ISO-8601 format with 'T' and 'Z'
        assertThat(event.timestamp()).contains("T");
        assertThat(event.timestamp()).endsWith("Z");
    }

    @Test
    void of_twoCallsShouldProduceDifferentTimestamps() throws InterruptedException {
        LoanRequestedEvent e1 = LoanRequestedEvent.of("l1", "u", "N", "e@m.com", 100.0, 1, 400, 5000.0);
        Thread.sleep(5);
        LoanRequestedEvent e2 = LoanRequestedEvent.of("l2", "u", "N", "e@m.com", 100.0, 1, 400, 5000.0);

        assertThat(e1.loanId()).isNotEqualTo(e2.loanId());
    }
}
