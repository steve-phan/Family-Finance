package com.familyfinance.security;

import com.familyfinance.domain.Member;
import com.familyfinance.domain.enums.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class MemberPrincipal implements UserDetails {

    private final Long memberId;
    private final Long familyId;
    private final String email;
    private final String passwordHash;
    private final Role role;
    private final boolean enabled;

    public MemberPrincipal(Member member) {
        this.memberId = member.getId();
        this.familyId = member.getFamilyId();
        this.email = member.getEmail();
        this.passwordHash = member.getPasswordHash();
        this.role = member.getRole();
        this.enabled = member.isEnabled();
    }

    public Long getMemberId() {
        return memberId;
    }

    public Long getFamilyId() {
        return familyId;
    }

    public Role getRole() {
        return role;
    }

    public boolean isParent() {
        return role == Role.PARENT;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return enabled;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
