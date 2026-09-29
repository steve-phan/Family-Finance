package com.familyfinance.service;

import com.familyfinance.api.dto.AddMemberRequest;
import com.familyfinance.api.dto.MemberResponse;
import com.familyfinance.domain.Account;
import com.familyfinance.domain.Family;
import com.familyfinance.domain.Member;
import com.familyfinance.domain.enums.AuditAction;
import com.familyfinance.exception.BusinessException;
import com.familyfinance.exception.ErrorCode;
import com.familyfinance.exception.ForbiddenOperationException;
import com.familyfinance.exception.NotFoundException;
import com.familyfinance.repository.AccountRepository;
import com.familyfinance.repository.FamilyRepository;
import com.familyfinance.repository.MemberRepository;
import com.familyfinance.security.MemberPrincipal;
import com.familyfinance.tenant.TenantContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FamilyService {

    private final FamilyRepository familyRepository;
    private final MemberRepository memberRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public FamilyService(
            FamilyRepository familyRepository,
            MemberRepository memberRepository,
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder,
            AuditService auditService
    ) {
        this.familyRepository = familyRepository;
        this.memberRepository = memberRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public Family requireFamily(Long familyId) {
        TenantContext.requireMatches(familyId);
        return familyRepository.findById(familyId)
                .filter(family -> family.getId().equals(familyId))
                .orElseThrow(() -> new NotFoundException("Family", familyId));
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> listMembers(MemberPrincipal principal) {
        return memberRepository.findByFamily_IdOrderByLastNameAscFirstNameAsc(principal.getFamilyId()).stream()
                .map(MemberResponse::from)
                .toList();
    }

    @Transactional
    public MemberResponse addMember(MemberPrincipal principal, AddMemberRequest request) {
        requireParent(principal);
        if (memberRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_TAKEN, "Email already registered");
        }
        Family family = requireFamily(principal.getFamilyId());
        Member actor = memberRepository.getReferenceById(principal.getMemberId());
        Member member = memberRepository.save(new Member(
                family,
                request.email(),
                passwordEncoder.encode(request.password()),
                request.firstName(),
                request.lastName(),
                request.role()
        ));
        accountRepository.save(new Account(member, family, family.getCurrency()));
        auditService.record(
                family,
                actor,
                AuditAction.MEMBER_ADDED,
                "Member",
                member.getId(),
                member.getDisplayName() + " (" + member.getRole() + ")"
        );
        return MemberResponse.from(member);
    }

    public static void requireParent(MemberPrincipal principal) {
        if (!principal.isParent()) {
            throw new ForbiddenOperationException("Only a parent can perform this action");
        }
    }
}
