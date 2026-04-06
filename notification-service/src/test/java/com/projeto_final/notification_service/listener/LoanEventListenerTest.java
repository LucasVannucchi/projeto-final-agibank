package com.projeto_final.notification_service.listener;

import com.projeto_final.notification_service.event.LoanApprovedEvent;
import com.projeto_final.notification_service.event.LoanRejectedEvent;
import com.projeto_final.notification_service.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LoanEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private LoanEventListener loanEventListener;

    // --- onLoanApproved ---

    @Test
    void onLoanApproved_shouldDelegateToNotificationService() {
        LoanApprovedEvent event = new LoanApprovedEvent(
                "loan-1", "user-1", "Alice", "a@b.com", 5000.0, 12, "c-1", "ts");

        loanEventListener.onLoanApproved(event);

        verify(notificationService).notifyApproved(event);
    }

    @Test
    void onLoanApproved_shouldPassExactEventToService() {
        LoanApprovedEvent event = new LoanApprovedEvent(
                "loan-exact", "user-exact", "Bob", "b@b.com", 1000.0, 6, "c-exact", "2024-01-01T00:00:00Z");

        loanEventListener.onLoanApproved(event);

        verify(notificationService).notifyApproved(event);
    }

    @Test
    void onLoanApproved_shouldHandleMinimalEvent() {
        LoanApprovedEvent event = new LoanApprovedEvent("l", "u", "N", "e@m.com", 0, 0, "c", "t");

        loanEventListener.onLoanApproved(event);

        verify(notificationService).notifyApproved(event);
    }

    // --- onLoanRejected ---

    @Test
    void onLoanRejected_shouldDelegateToNotificationService() {
        LoanRejectedEvent event = new LoanRejectedEvent(
                "loan-2", "user-2", "Carol", "c@b.com", 2000.0, "Low score", "ts");

        loanEventListener.onLoanRejected(event);

        verify(notificationService).notifyRejected(event);
    }

    @Test
    void onLoanRejected_shouldPassExactEventToService() {
        LoanRejectedEvent event = new LoanRejectedEvent(
                "loan-rej", "user-rej", "Dave", "d@b.com", 3000.0, "Existing contract", "2024-01-01T00:00:00Z");

        loanEventListener.onLoanRejected(event);

        verify(notificationService).notifyRejected(event);
    }

    @Test
    void onLoanRejected_shouldHandleEventWithLongReasonMessage() {
        LoanRejectedEvent event = new LoanRejectedEvent(
                "loan-long", "user-long", "Eve", "e@b.com", 5000.0,
                "User already has an active contract and score is below minimum threshold", "ts");

        loanEventListener.onLoanRejected(event);

        verify(notificationService).notifyRejected(event);
    }
}
