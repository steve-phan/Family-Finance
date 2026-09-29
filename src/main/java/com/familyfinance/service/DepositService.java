package com.familyfinance.service;

import com.familyfinance.api.dto.AccountResponse;
import com.familyfinance.domain.Account;
import com.familyfinance.domain.Member;
import com.familyfinance.domain.enums.AuditAction;
import com.familyfinance.exception.ForbiddenOperationException;
import com.familyfinance.security.MemberPrincipal;
import com.familyfinance.tx.MoneyTransaction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DepositService {

    private final MemberLookup memberLookup;
    private final MoneyRules moneyRules;
    private final AuditService auditService;

    public DepositService(MemberLookup memberLookup, MoneyRules moneyRules, AuditService auditService) {
        this.memberLookup = memberLookup;
        this.moneyRules = moneyRules;
        this.auditService = auditService;
    }

    @MoneyTransaction
    public AccountResponse deposit(MemberPrincipal principal, BigDecimal rawAmount) {
        if (!principal.isParent()) {
            throw new ForbiddenOperationException("Only a parent can deposit external funds");
        }
        BigDecimal amount = moneyRules.normalize(rawAmount);
        Member parent = memberLookup.requireMember(principal.getMemberId());
        Account account = memberLookup.lockAccount(principal.getMemberId());
        account.credit(amount);
        auditService.record(
                parent.getFamily(),
                parent,
                AuditAction.EXTERNAL_DEPOSIT,
                "Account",
                account.getId(),
                "External deposit " + amount
        );
        return AccountResponse.from(account);
    }
}
