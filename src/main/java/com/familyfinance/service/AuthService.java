package com.familyfinance.service;

import com.familyfinance.api.dto.AuthResponse;
import com.familyfinance.api.dto.LoginRequest;
import com.familyfinance.api.dto.MemberResponse;
import com.familyfinance.api.dto.RegisterFamilyRequest;
import com.familyfinance.domain.Family;
import com.familyfinance.domain.Member;
import com.familyfinance.exception.BusinessException;
import com.familyfinance.exception.ErrorCode;
import com.familyfinance.repository.MemberRepository;
import com.familyfinance.security.JwtService;
import com.familyfinance.security.MemberPrincipal;
import com.familyfinance.tenant.TenantContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final FamilyRegistrationService familyRegistrationService;

    public AuthService(
            MemberRepository memberRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            FamilyRegistrationService familyRegistrationService
    ) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.familyRegistrationService = familyRegistrationService;
    }

    public AuthResponse registerFamily(RegisterFamilyRequest request) {
        if (memberRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_TAKEN, "Email already registered");
        }
        Family family = familyRegistrationService.createFamily(request.familyName(), request.currencyOrDefault());
        return TenantContext.call(family.getId(), () -> familyRegistrationService.provisionParent(family, request));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS, "Invalid email or password"));
        if (!member.isEnabled() || !passwordEncoder.matches(request.password(), member.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "Invalid email or password");
        }
        return new AuthResponse(jwtService.issueToken(new MemberPrincipal(member)), MemberResponse.from(member));
    }
}
