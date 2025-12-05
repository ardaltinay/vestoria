package io.vestoria.service;

import io.vestoria.repository.MarketRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CleanupService {

    private final MarketRepository marketRepository;

    /**
     * Clean up inactive market listings older than 24 hours Runs every day at 03:00
     * AM
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanInactiveMarketListings() {
        log.info("Starting inactive market listings cleanup...");
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        marketRepository.deleteByIsActiveFalseAndUpdatedTimeBefore(threshold);
        log.info("Inactive market listings cleanup completed.");
    }

}
