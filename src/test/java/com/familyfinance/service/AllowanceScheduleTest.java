package com.familyfinance.service;

import com.familyfinance.domain.enums.AllowancePeriod;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class AllowanceScheduleTest {

    @Test
    void weeklyAddsSevenDays() {
        Instant from = Instant.parse("2026-01-01T00:00:00Z");
        assertThat(AllowanceService.nextRun(AllowancePeriod.WEEKLY, from))
                .isEqualTo(Instant.parse("2026-01-08T00:00:00Z"));
    }

    @Test
    void monthlyAddsThirtyDays() {
        Instant from = Instant.parse("2026-01-01T00:00:00Z");
        assertThat(AllowanceService.nextRun(AllowancePeriod.MONTHLY, from))
                .isEqualTo(Instant.parse("2026-01-31T00:00:00Z"));
    }
}
