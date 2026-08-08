package com.posdb.sync.job;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.logging.Log;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.sql.Timestamp;

@ApplicationScoped
public class DiscountAmountBatchJob {

    @Inject
    private EntityManager entityManager;

    private LocalDateTime lastRunTime;

    @Scheduled(cron = "0 0 6 * * ?") // Runs daily at 6 AM
    @Transactional
    public void updateDiscountAmountUsed() {
        try {
            lastRunTime = getLastBatchRunTime();

            String sql = "UPDATE order_headers " +
                    "SET discount_amount_used = ROUND((amount_due * discount_amount) / (100.0 - discount_amount), 2) " +
                    "WHERE discount_amount IS NOT NULL " +
                    "AND updated_at > :lastRunTime";

            entityManager.createNativeQuery(sql)
                    .setParameter("lastRunTime", Timestamp.valueOf(lastRunTime))
                    .executeUpdate();

            lastRunTime = LocalDateTime.now();

            Log.info("Discount amount batch job completed successfully at: " + lastRunTime);
        } catch (Exception e) {
            Log.error("Error in discount amount batch job: " + e.getMessage(), e);
        }
    }

    private LocalDateTime getLastBatchRunTime() {
        // Returns stored last run time or fallback to 24 hours ago
        return lastRunTime != null ? lastRunTime : LocalDateTime.now().minusHours(24);
    }
}
