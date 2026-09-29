package com.familyfinance.service;

import com.familyfinance.api.dto.AuthResponse;
import com.familyfinance.api.dto.MemberResponse;
import com.familyfinance.api.dto.RegisterFamilyRequest;
import com.familyfinance.domain.Account;
import com.familyfinance.domain.Family;
import com.familyfinance.domain.Member;
import com.familyfinance.domain.enums.AuditAction;
import com.familyfinance.domain.enums.Role;
import com.familyfinance.repository.AccountRepository;
import com.familyfinance.repository.FamilyRepository;
import com.familyfinance.repository.MemberRepository;
import com.familyfinance.security.JwtService;
import com.familyfinance.security.MemberPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FamilyRegistrationService {

    private final FamilyRepository familyRepository;
    private final MemberRepository memberRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditService auditService;

    public FamilyRegistrationService(
            FamilyRepository familyRepository,
            MemberRepository memberRepository,
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuditService auditService
    ) {
        this.familyRepository = familyRepository;
        this.memberRepository = memberRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditService = auditService;
    }

    @Transactional
    public Family createFamily(String name, String currency) {
        return familyRepository.saveAndFlush(new Family(name, currency));
    }

    @Transactional
    public AuthResponse provisionParent(Family family, RegisterFamilyRequest request) {
        Member parent = memberRepository.save(new Member(
                family,
                request.email(),
                passwordEncoder.encode(request.password()),
                request.firstName(),
                request.lastName(),
                Role.PARENT
        ));
        accountRepository.save(new Account(parent, family, family.getCurrency()));
        auditService.record(family, parent, AuditAction.FAMILY_CREATED, "Family", family.getId(), family.getName());
        String token = jwtService.issueToken(new MemberPrincipal(parent));
        return new AuthResponse(token, MemberResponse.from(parent));
    }
}
