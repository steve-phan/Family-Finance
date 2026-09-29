package com.familyfinance.service;

import com.familyfinance.api.dto.AccountResponse;
import com.familyfinance.api.dto.FamilyResponse;
import com.familyfinance.api.dto.MeResponse;
import com.familyfinance.api.dto.MemberResponse;
import com.familyfinance.domain.Account;
import com.familyfinance.domain.Family;
import com.familyfinance.domain.Member;
import com.familyfinance.security.MemberPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MeService {

    private final MemberLookup memberLookup;
    private final FamilyService familyService;

    public MeService(MemberLookup memberLookup, FamilyService familyService) {
        this.memberLookup = memberLookup;
        this.familyService = familyService;
    }

    @Transactional(readOnly = true)
    public MeResponse me(MemberPrincipal principal) {
        Member member = memberLookup.requireMember(principal.getMemberId());
        Family family = familyService.requireFamily(principal.getFamilyId());
        Account account = memberLookup.requireAccount(principal.getMemberId());
        return new MeResponse(MemberResponse.from(member), FamilyResponse.from(family), AccountResponse.from(account));
    }
}
