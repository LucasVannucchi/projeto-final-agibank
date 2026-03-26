package com.projeto_final.loan_service.service;

import com.projeto_final.loan_service.client.CreditAnalysisClient;
import com.projeto_final.loan_service.dto.*;
import com.projeto_final.loan_service.event.LoanRequestedEvent;
import com.projeto_final.loan_service.exception.InsufficientLimitException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanService {

    private static final String LIMIT_KEY_FORMAT = "user:%s:limit";
    private static final String SCORE_KEY_FORMAT  = "user:%s:score";
    private static final Duration CACHE_TTL       = Duration.ofMinutes(30);
    private static final String TOPIC             = "loan-requested";

    private final StringRedisTemplate        redisTemplate;
    private final CreditAnalysisClient       creditAnalysisClient;
    private final KafkaTemplate<String, LoanRequestedEvent> kafkaTemplate;

    public LoanResponse requestLoan(LoanRequest request) {
        log.info("Processing loan request for userId={} amount={}", request.userId(), request.amount());

        String limitKey = String.format(LIMIT_KEY_FORMAT, request.userId());
        String scoreKey = String.format(SCORE_KEY_FORMAT,  request.userId());

        String cachedLimit = redisTemplate.opsForValue().get(limitKey);
        String cachedScore = redisTemplate.opsForValue().get(scoreKey);

        double limit;
        int score;

        if (cachedLimit != null && cachedScore != null) {
            limit = Double.parseDouble(cachedLimit);
            score = Integer.parseInt(cachedScore);
            log.debug("Cache hit for userId={}: limit={} score={}", request.userId(), limit, score);
        } else {
            log.debug("Cache miss for userId={} — calling credit-analysis-service", request.userId());
            CreditAnalysisResponse analysis = creditAnalysisClient.analyze(request.userId());
            limit = analysis.limit();
            score = analysis.score();
            redisTemplate.opsForValue().set(limitKey, String.valueOf(limit), CACHE_TTL);
            redisTemplate.opsForValue().set(scoreKey, String.valueOf(score), CACHE_TTL);
            log.info("Credit analysis cached for userId={}: limit={} score={}", request.userId(), limit, score);
        }

        if (request.amount() > limit) {
            log.warn("Insufficient limit for userId={}: requested={} limit={}", request.userId(), request.amount(), limit);
            throw new InsufficientLimitException(
                    String.format("Requested amount %.2f exceeds credit limit %.2f", request.amount(), limit));
        }

        String loanId = UUID.randomUUID().toString();
        LoanRequestedEvent event = LoanRequestedEvent.of(
                loanId, request.userId(), request.name(), request.email(),
                request.amount(), request.installments(), score, limit);

        kafkaTemplate.send(TOPIC, loanId, event);
        log.info("Loan event published to topic={} loanId={}", TOPIC, loanId);

        return LoanResponse.accepted(loanId);
    }
}
