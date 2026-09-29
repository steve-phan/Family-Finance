package com.familyfinance.domain;

import com.familyfinance.domain.enums.AllowancePeriod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "allowance_rule")
public class AllowanceRule extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parent_id", nullable = false)
    private Member parent;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "child_id", nullable = false)
    private Member child;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AllowancePeriod period;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "next_run_at", nullable = false)
    private Instant nextRunAt;

    protected AllowanceRule() {
    }

    public AllowanceRule(Family family, Member parent, Member child, BigDecimal amount, AllowancePeriod period, Instant nextRunAt) {
        bindFamily(family);
        this.parent = parent;
        this.child = child;
        this.amount = amount;
        this.period = period;
        this.nextRunAt = nextRunAt;
    }

    public Member getParent() {
        return parent;
    }

    public Member getChild() {
        return child;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public AllowancePeriod getPeriod() {
        return period;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getNextRunAt() {
        return nextRunAt;
    }

    public void disable() {
        this.active = false;
    }

    public void scheduleNext(Instant nextRunAt) {
        this.nextRunAt = nextRunAt;
    }
}
