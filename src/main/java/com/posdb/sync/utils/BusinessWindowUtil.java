package com.posdb.sync.utils;

import java.time.*;

public class BusinessWindowUtil {

    public static BusinessWindow getBusinessWindow(LocalTime openTime, LocalTime closeTime, LocalDate referenceDate, String timeZone) {
        LocalDateTime startDateTime;
        LocalDateTime endDateTime;
        if (closeTime.isAfter(openTime)) {
            // Same-day closing (10:00 → 23:00)
            startDateTime = referenceDate.atTime(openTime);
            endDateTime   = referenceDate.atTime(closeTime);
        } else {
            // Cross-midnight closing (10:00 → 03:00 next day)
            startDateTime = referenceDate.atTime(openTime);
            endDateTime   = referenceDate.plusDays(1).atTime(closeTime);
        }
        ZoneId zoneId = getZoneId(timeZone);
        OffsetDateTime startDate = startDateTime.atZone(zoneId).toOffsetDateTime();
        OffsetDateTime endDate   = endDateTime.atZone(zoneId).toOffsetDateTime();
        return new BusinessWindow(startDate, endDate);
    }

    public record BusinessWindow(OffsetDateTime start, OffsetDateTime end) {

    }

    private static ZoneId getZoneId(String timeZone) {
        try {
            return ZoneId.of(timeZone);
        } catch (Exception e) {
            return ZoneId.of("Asia/Dubai");
        }
    }
}