package com.posdb.sync.utils;

import lombok.Getter;

import java.time.*;
import java.util.Date;

public class BusinessWindowUtil {

    public static BusinessWindow getYesterdayWindow(
            LocalTime openTime,
            LocalTime closeTime) {

        LocalDate yesterday = LocalDate.now().minusDays(1);

        LocalDateTime startDateTime;
        LocalDateTime endDateTime;

        if (closeTime.isAfter(openTime)) {
            // Same-day closing (10:00 → 23:00)
            startDateTime = yesterday.atTime(openTime);
            endDateTime   = yesterday.atTime(closeTime);
        } else {
            // Cross-midnight closing (10:00 → 03:00 next day)
            startDateTime = yesterday.atTime(openTime);
            endDateTime   = yesterday.plusDays(1).atTime(closeTime);
        }

        // Convert to java.util.Date
        Date startDate = Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant());
        Date endDate   = Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant());

        return new BusinessWindow(startDate, endDate);
    }

    @Getter
    public static class BusinessWindow {
        private final Date start;
        private final Date end;

        public BusinessWindow(Date start, Date end) {
            this.start = start;
            this.end = end;
        }

    }
}