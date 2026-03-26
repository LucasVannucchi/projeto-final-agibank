package com.projeto_final.notification_service.service;

import com.projeto_final.notification_service.event.LoanApprovedEvent;
import com.projeto_final.notification_service.event.LoanRejectedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class NotificationService {

    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String userId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(()    -> removeEmitter(userId, emitter));
        emitter.onError(e      -> removeEmitter(userId, emitter));

        log.info("SSE subscription registered for userId={}", userId);
        return emitter;
    }

    public void notifyApproved(LoanApprovedEvent event) {
        String message = String.format(
                "✅ Parabéns %s! Seu empréstimo de R$ %.2f em %d parcelas foi APROVADO. ContractId: %s",
                event.name(), event.amount(), event.installments(), event.contractId());
        sendToUser(event.userId(), "loan-approved", message);
    }

    public void notifyRejected(LoanRejectedEvent event) {
        String message = String.format(
                "❌ Olá %s. Infelizmente seu empréstimo de R$ %.2f foi NEGADO. Motivo: %s",
                event.name(), event.amount(), event.reason());
        sendToUser(event.userId(), "loan-rejected", message);
    }

    private void sendToUser(String userId, String eventType, String message) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters == null || userEmitters.isEmpty()) {
            log.warn("No active SSE subscribers for userId={} — event={}", userId, eventType);
            return;
        }

        List<SseEmitter> dead = new CopyOnWriteArrayList<>();
        for (SseEmitter emitter : userEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventType)
                        .data(message));
                log.info("SSE sent to userId={} event={}", userId, eventType);
            } catch (IOException e) {
                log.warn("Failed to send SSE to userId={}: {}", userId, e.getMessage());
                dead.add(emitter);
            }
        }
        userEmitters.removeAll(dead);
    }

    private void removeEmitter(String userId, SseEmitter emitter) {
        List<SseEmitter> list = emitters.get(userId);
        if (list != null) list.remove(emitter);
        log.debug("SSE emitter removed for userId={}", userId);
    }
}
