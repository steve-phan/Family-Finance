package com.familyfinance.service;

import com.familyfinance.domain.AllowanceRule;
import com.familyfinance.domain.enums.AuditAction;
import com.familyfinance.domain.enums.TransferType;
import com.familyfinance.exception.NotFoundException;
import com.familyfinance.repository.AllowanceRuleRepository;
import com.familyfinance.tx.MoneyTransaction;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

@Service
public class AllowancePayoutService {

    private final AllowanceRuleRepository allowanceRuleRepository;
    private final TransferService transferService;
    private final AuditService auditService;
    private final Clock clock;

    public AllowancePayoutService(
            AllowanceRuleRepository allowanceRuleRepository,
            TransferService transferService,
            AuditService auditService,
            Clock clock
    ) {
        this.allowanceRuleRepository = allowanceRuleRepository;
        this.transferService = transferService;
        this.auditService = auditService;
        this.clock = clock;
    }

    @MoneyTransaction.Isolated
    public void payOne(Long ruleId) {
        AllowanceRule rule = allowanceRuleRepository.findById(ruleId)
                .orElseThrow(() -> new NotFoundException("AllowanceRule", ruleId));
        if (!rule.isActive()) {
            return;
        }
        transferService.sendTyped(
                rule.getParent().getId(),
                rule.getChild().getId(),
                rule.getAmount(),
                "Allowance " + rule.getPeriod(),
                TransferType.ALLOWANCE,
                rule.getParent()
        );
        rule.scheduleNext(AllowanceService.nextRun(rule.getPeriod(), Instant.now(clock)));
        auditService.record(
                rule.getFamily(),
                rule.getParent(),
                AuditAction.ALLOWANCE_PAID,
                "AllowanceRule",
                rule.getId(),
                "Paid " + rule.getAmount()
        );
    }
}
