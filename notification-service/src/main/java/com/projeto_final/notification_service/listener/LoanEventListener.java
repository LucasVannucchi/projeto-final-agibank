package com.projeto_final.notification_service.listener;

import com.projeto_final.notification_service.event.LoanApprovedEvent;
import com.projeto_final.notification_service.event.LoanRejectedEvent;
import com.projeto_final.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoanEventListener {

    private final NotificationService notificationService;

    @KafkaListener(topics = "loan-approved", groupId = "notification-service-group",
                   containerFactory = "approvedFactory")
    public void onLoanApproved(LoanApprovedEvent event) {
        log.info("Received loan-approved: loanId={} userId={}", event.loanId(), event.userId());
        notificationService.notifyApproved(event);
    }

    @KafkaListener(topics = "loan-rejected", groupId = "notification-service-group",
                   containerFactory = "rejectedFactory")
    public void onLoanRejected(LoanRejectedEvent event) {
        log.info("Received loan-rejected: loanId={} userId={}", event.loanId(), event.userId());
        notificationService.notifyRejected(event);
    }
}
