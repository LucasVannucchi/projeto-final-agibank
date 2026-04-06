package com.projeto_final.notification_service.controller;

import com.projeto_final.notification_service.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    @Test
    void subscribe_shouldDelegateToServiceAndReturnEmitter() {
        SseEmitter expected = new SseEmitter();
        when(notificationService.subscribe("user-1")).thenReturn(expected);

        SseEmitter result = notificationController.subscribe("user-1");

        assertThat(result).isSameAs(expected);
        verify(notificationService).subscribe("user-1");
    }

    @Test
    void subscribe_shouldReturnNonNullEmitter() {
        when(notificationService.subscribe("user-2")).thenReturn(new SseEmitter());

        SseEmitter result = notificationController.subscribe("user-2");

        assertThat(result).isNotNull();
    }

    @Test
    void subscribe_shouldPassExactUserIdToService() {
        String userId = "exact-user-id-xyz";
        when(notificationService.subscribe(userId)).thenReturn(new SseEmitter());

        notificationController.subscribe(userId);

        verify(notificationService).subscribe(userId);
    }

    @Test
    void subscribe_shouldReturnDistinctEmittersForDifferentUsers() {
        SseEmitter e1 = new SseEmitter();
        SseEmitter e2 = new SseEmitter();
        when(notificationService.subscribe("user-A")).thenReturn(e1);
        when(notificationService.subscribe("user-B")).thenReturn(e2);

        SseEmitter r1 = notificationController.subscribe("user-A");
        SseEmitter r2 = notificationController.subscribe("user-B");

        assertThat(r1).isNotSameAs(r2);
    }
}
