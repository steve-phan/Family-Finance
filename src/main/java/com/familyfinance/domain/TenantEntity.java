package com.familyfinance.domain;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.TenantId;

@MappedSuperclass
public abstract class TenantEntity extends BaseEntity {

    @TenantId
    @Column(name = "family_id", nullable = false)
    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "family_id", insertable = false, updatable = false)
    private Family family;

    protected void bindFamily(Family family) {
        this.family = family;
        this.tenantId = family.getId();
    }

    public Family getFamily() {
        return family;
    }

    public Long getFamilyId() {
        if (tenantId != null) {
            return tenantId;
        }
        return family == null ? null : family.getId();
    }
}
