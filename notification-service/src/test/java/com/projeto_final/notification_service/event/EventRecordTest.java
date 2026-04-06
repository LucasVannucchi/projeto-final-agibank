package com.projeto_final.notification_service.event;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EventRecordTest {

    @Test
    void loanApprovedEvent_shouldHoldAllFields() {
        LoanApprovedEvent event = new LoanApprovedEvent(
                "loan-1", "user-1", "Alice", "alice@mail.com", 5000.0, 12, "c-1", "2024-01-01T00:00:00Z");

        assertThat(event.loanId()).isEqualTo("loan-1");
        assertThat(event.userId()).isEqualTo("user-1");
        assertThat(event.name()).isEqualTo("Alice");
        assertThat(event.email()).isEqualTo("alice@mail.com");
        assertThat(event.amount()).isEqualTo(5000.0);
        assertThat(event.installments()).isEqualTo(12);
        assertThat(event.contractId()).isEqualTo("c-1");
        assertThat(event.timestamp()).isEqualTo("2024-01-01T00:00:00Z");
    }

    @Test
    void loanRejectedEvent_shouldHoldAllFields() {
        LoanRejectedEvent event = new LoanRejectedEvent(
                "loan-2", "user-2", "Bob", "bob@mail.com", 3000.0, "Low score", "2024-06-01T00:00:00Z");

        assertThat(event.loanId()).isEqualTo("loan-2");
        assertThat(event.userId()).isEqualTo("user-2");
        assertThat(event.name()).isEqualTo("Bob");
        assertThat(event.email()).isEqualTo("bob@mail.com");
        assertThat(event.amount()).isEqualTo(3000.0);
        assertThat(event.reason()).isEqualTo("Low score");
        assertThat(event.timestamp()).isEqualTo("2024-06-01T00:00:00Z");
    }

    @Test
    void loanApprovedEvent_equalityBasedOnAllComponents() {
        LoanApprovedEvent e1 = new LoanApprovedEvent("l", "u", "N", "e@m.com", 100.0, 1, "c", "ts");
        LoanApprovedEvent e2 = new LoanApprovedEvent("l", "u", "N", "e@m.com", 100.0, 1, "c", "ts");

        assertThat(e1).isEqualTo(e2);
    }

    @Test
    void loanRejectedEvent_equalityBasedOnAllComponents() {
        LoanRejectedEvent e1 = new LoanRejectedEvent("l", "u", "N", "e@m.com", 100.0, "reason", "ts");
        LoanRejectedEvent e2 = new LoanRejectedEvent("l", "u", "N", "e@m.com", 100.0, "reason", "ts");

        assertThat(e1).isEqualTo(e2);
    }
}
