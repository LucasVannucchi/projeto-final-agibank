package com.projeto_final.workflow.event;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EventTest {

    @Test
    void loanApprovedEvent_fromFactory_shouldPopulateAllFieldsFromRequest() {
        LoanRequestedEvent req = new LoanRequestedEvent(
                "loan-1", "user-1", "Alice", "alice@mail.com", 3000.0, 12, 500, 6000.0, "ts");

        LoanApprovedEvent approved = LoanApprovedEvent.from(req, "contract-abc");

        assertThat(approved.loanId()).isEqualTo("loan-1");
        assertThat(approved.userId()).isEqualTo("user-1");
        assertThat(approved.name()).isEqualTo("Alice");
        assertThat(approved.email()).isEqualTo("alice@mail.com");
        assertThat(approved.amount()).isEqualTo(3000.0);
        assertThat(approved.installments()).isEqualTo(12);
        assertThat(approved.contractId()).isEqualTo("contract-abc");
        assertThat(approved.timestamp()).isNotBlank();
    }

    @Test
    void loanRejectedEvent_fromFactory_shouldPopulateAllFieldsFromRequest() {
        LoanRequestedEvent req = new LoanRequestedEvent(
                "loan-2", "user-2", "Bob", "bob@mail.com", 1000.0, 6, 300, 4000.0, "ts");

        LoanRejectedEvent rejected = LoanRejectedEvent.from(req, "Insufficient score");

        assertThat(rejected.loanId()).isEqualTo("loan-2");
        assertThat(rejected.userId()).isEqualTo("user-2");
        assertThat(rejected.name()).isEqualTo("Bob");
        assertThat(rejected.email()).isEqualTo("bob@mail.com");
        assertThat(rejected.reason()).isEqualTo("Insufficient score");
        assertThat(rejected.timestamp()).isNotBlank();
    }

    @Test
    void loanRequestedEvent_shouldHoldAllFields() {
        LoanRequestedEvent event = new LoanRequestedEvent(
                "l1", "u1", "Name", "email@x.com", 500.0, 3, 400, 8000.0, "2024-01-01");

        assertThat(event.loanId()).isEqualTo("l1");
        assertThat(event.score()).isEqualTo(400);
        assertThat(event.limit()).isEqualTo(8000.0);
    }
}
