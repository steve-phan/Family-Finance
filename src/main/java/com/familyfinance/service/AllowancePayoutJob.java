package com.familyfinance.service;

import com.familyfinance.repository.AllowanceRuleRepository;
import com.familyfinance.repository.DueAllowance;
import com.familyfinance.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

@Component
public class AllowancePayoutJob {

    private static final Logger log = LoggerFactory.getLogger(AllowancePayoutJob.class);

    private final AllowanceRuleRepository allowanceRuleRepository;
    private final AllowancePayoutService allowancePayoutService;
    private final Clock clock;

    public AllowancePayoutJob(
            AllowanceRuleRepository allowanceRuleRepository,
            AllowancePayoutService allowancePayoutService,
            Clock clock
    ) {
        this.allowanceRuleRepository = allowanceRuleRepository;
        this.allowancePayoutService = allowancePayoutService;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${app.allowance.poll-ms:60000}")
    public void payDueAllowances() {
        for (DueAllowance due : allowanceRuleRepository.findDue(Instant.now(clock))) {
            try {
                TenantContext.run(due.getFamilyId(), () -> allowancePayoutService.payOne(due.getId()));
            } catch (Exception ex) {
                log.warn("Failed to pay allowance {}: {}", due.getId(), ex.getMessage());
            }
        }
    }
}
