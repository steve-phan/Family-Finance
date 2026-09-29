package com.familyfinance.service;

import com.familyfinance.api.dto.AllowanceResponse;
import com.familyfinance.domain.AllowanceRule;
import com.familyfinance.domain.Member;
import com.familyfinance.domain.enums.AllowancePeriod;
import com.familyfinance.domain.enums.AuditAction;
import com.familyfinance.exception.ForbiddenOperationException;
import com.familyfinance.exception.NotFoundException;
import com.familyfinance.repository.AllowanceRuleRepository;
import com.familyfinance.security.MemberPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class AllowanceService {

    private final AllowanceRuleRepository allowanceRuleRepository;
    private final MemberLookup memberLookup;
    private final MoneyRules moneyRules;
    private final AuditService auditService;
    private final Clock clock;

    public AllowanceService(
            AllowanceRuleRepository allowanceRuleRepository,
            MemberLookup memberLookup,
            MoneyRules moneyRules,
            AuditService auditService,
            Clock clock
    ) {
        this.allowanceRuleRepository = allowanceRuleRepository;
        this.memberLookup = memberLookup;
        this.moneyRules = moneyRules;
        this.auditService = auditService;
        this.clock = clock;
    }

    @Transactional
    public AllowanceResponse create(MemberPrincipal principal, Long childId, BigDecimal rawAmount, AllowancePeriod period) {
        FamilyService.requireParent(principal);
        BigDecimal amount = moneyRules.normalize(rawAmount);
        Member parent = memberLookup.requireMember(principal.getMemberId());
        Member child = memberLookup.requireMember(childId);
        if (!child.isChild()) {
            throw new ForbiddenOperationException("Allowances can only be assigned to children");
        }
        memberLookup.requireSameFamily(parent, child);
        Instant nextRunAt = nextRun(period, Instant.now(clock));
        AllowanceRule rule = allowanceRuleRepository.save(
                new AllowanceRule(parent.getFamily(), parent, child, amount, period, nextRunAt)
        );
        auditService.record(
                parent.getFamily(),
                parent,
                AuditAction.ALLOWANCE_CREATED,
                "AllowanceRule",
                rule.getId(),
                amount + " " + period + " to " + child.getDisplayName()
        );
        return AllowanceResponse.from(rule);
    }

    @Transactional
    public AllowanceResponse disable(MemberPrincipal principal, Long ruleId) {
        FamilyService.requireParent(principal);
        AllowanceRule rule = allowanceRuleRepository.findById(ruleId)
                .orElseThrow(() -> new NotFoundException("AllowanceRule", ruleId));
        rule.disable();
        auditService.record(
                rule.getFamily(),
                memberLookup.requireMember(principal.getMemberId()),
                AuditAction.ALLOWANCE_DISABLED,
                "AllowanceRule",
                rule.getId(),
                "Disabled"
        );
        return AllowanceResponse.from(rule);
    }

    @Transactional(readOnly = true)
    public List<AllowanceResponse> listActive(MemberPrincipal principal) {
        return allowanceRuleRepository.findByFamily_IdAndActiveTrue(principal.getFamilyId()).stream()
                .map(AllowanceResponse::from)
                .toList();
    }

    static Instant nextRun(AllowancePeriod period, Instant from) {
        return switch (period) {
            case WEEKLY -> from.plus(7, ChronoUnit.DAYS);
            case MONTHLY -> from.plus(30, ChronoUnit.DAYS);
        };
    }
}
