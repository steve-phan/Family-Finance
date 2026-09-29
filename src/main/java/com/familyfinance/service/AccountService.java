package com.familyfinance.service;

import com.familyfinance.api.dto.AccountResponse;
import com.familyfinance.domain.Account;
import com.familyfinance.exception.ForbiddenOperationException;
import com.familyfinance.security.MemberPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final MemberLookup memberLookup;

    public AccountService(MemberLookup memberLookup) {
        this.memberLookup = memberLookup;
    }

    @Transactional(readOnly = true)
    public AccountResponse myAccount(MemberPrincipal principal) {
        return AccountResponse.from(memberLookup.requireAccount(principal.getMemberId()));
    }

    @Transactional(readOnly = true)
    public AccountResponse accountOf(MemberPrincipal principal, Long memberId) {
        Account account = memberLookup.requireAccount(memberId);
        if (!principal.isParent() && !principal.getMemberId().equals(memberId)) {
            throw new ForbiddenOperationException("Children can only view their own account");
        }
        return AccountResponse.from(account);
    }
}
