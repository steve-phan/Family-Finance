package com.familyfinance.service;

import com.familyfinance.domain.Account;
import com.familyfinance.domain.Member;
import com.familyfinance.exception.BusinessException;
import com.familyfinance.exception.ErrorCode;
import com.familyfinance.exception.NotFoundException;
import com.familyfinance.repository.AccountRepository;
import com.familyfinance.repository.MemberRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class MemberLookup {

    private final MemberRepository memberRepository;
    private final AccountRepository accountRepository;

    public MemberLookup(MemberRepository memberRepository, AccountRepository accountRepository) {
        this.memberRepository = memberRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public Member requireMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member", memberId));
    }

    public void requireSameFamily(Member a, Member b) {
        if (!a.getFamilyId().equals(b.getFamilyId())) {
            throw new BusinessException(ErrorCode.DIFFERENT_FAMILY, "Members must belong to the same family");
        }
    }

    public void requireEnabled(Member member) {
        if (!member.isEnabled()) {
            throw new BusinessException(ErrorCode.MEMBER_DISABLED, "Member is disabled: " + member.getId());
        }
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Account lockAccount(Long memberId) {
        return accountRepository.findByMemberIdForUpdate(memberId)
                .orElseThrow(() -> new NotFoundException("Account for member", memberId));
    }

    @Transactional(readOnly = true)
    public Account requireAccount(Long memberId) {
        return accountRepository.findByMember_Id(memberId)
                .orElseThrow(() -> new NotFoundException("Account for member", memberId));
    }
}
