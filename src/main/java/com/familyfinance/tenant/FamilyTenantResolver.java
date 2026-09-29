package com.familyfinance.tenant;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

@Component
public class FamilyTenantResolver implements CurrentTenantIdentifierResolver<Long> {

    static final Long ROOT = 0L;

    @Override
    public Long resolveCurrentTenantIdentifier() {
        return TenantContext.current().orElse(ROOT);
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }

    @Override
    public boolean isRoot(Long tenantId) {
        return ROOT.equals(tenantId);
    }
}
