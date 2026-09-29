package com.familyfinance.domain;

import com.familyfinance.domain.enums.TransferRequestStatus;
import com.familyfinance.exception.BusinessException;
import com.familyfinance.exception.ErrorCode;
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
@Table(name = "transfer_request")
public class TransferRequest extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private Member requester;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payer_id", nullable = false)
    private Member payer;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransferRequestStatus status;

    @Column(length = 255)
    private String message;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    protected TransferRequest() {
    }

    public TransferRequest(Family family, Member requester, Member payer, BigDecimal amount, String message) {
        bindFamily(family);
        this.requester = requester;
        this.payer = payer;
        this.amount = amount;
        this.message = message;
        this.status = TransferRequestStatus.PENDING;
    }

    public Member getRequester() {
        return requester;
    }

    public Member getPayer() {
        return payer;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransferRequestStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void approve() {
        requirePending();
        this.status = TransferRequestStatus.APPROVED;
        this.resolvedAt = Instant.now();
    }

    public void reject() {
        requirePending();
        this.status = TransferRequestStatus.REJECTED;
        this.resolvedAt = Instant.now();
    }

    public void cancel() {
        requirePending();
        this.status = TransferRequestStatus.CANCELLED;
        this.resolvedAt = Instant.now();
    }

    public boolean isPending() {
        return status == TransferRequestStatus.PENDING;
    }

    private void requirePending() {
        if (!isPending()) {
            throw new BusinessException(ErrorCode.REQUEST_NOT_PENDING, "Transfer request is no longer pending");
        }
    }
}
