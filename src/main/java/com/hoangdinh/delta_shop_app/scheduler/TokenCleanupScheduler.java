package com.hoangdinh.delta_shop_app.scheduler;

import com.hoangdinh.delta_shop_app.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {

    private final VerificationTokenRepository verificationTokenRepository;

    /**
     * Delete expired tokens every hour
     */
    @Scheduled(cron = "0 0 * * * *") // Run at the beginning of every hour
    @Transactional
    public void cleanExpiredTokens() {
        int deleted = verificationTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        if (deleted > 0) {
            log.info("Scheduled cleanup: deleted {} expired verification tokens", deleted);
        }
    }

    /**
     * Delete old used tokens daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * *") // Run at 2 AM every day
    @Transactional
    public void cleanOldUsedTokens() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        int deleted = verificationTokenRepository.deleteOldUsedTokens(thirtyDaysAgo);
        if (deleted > 0) {
            log.info("Scheduled cleanup: deleted {} old used tokens", deleted);
        }
    }
}