package com.familyfinance.domain;

import com.familyfinance.domain.enums.TransferStatus;
import com.familyfinance.domain.enums.TransferType;
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
@Table(name = "money_transfer")
public class MoneyTransfer extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_account_id", nullable = false)
    private Account sourceAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_account_id", nullable = false)
    private Account targetAccount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TransferType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransferStatus status;

    @Column(length = 255)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "initiated_by_id", nullable = false)
    private Member initiatedBy;

    @Column(name = "idempotency_key", length = 64)
    private String idempotencyKey;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected MoneyTransfer() {
    }

    public static MoneyTransfer completed(
            Family family,
            Account source,
            Account target,
            BigDecimal amount,
            TransferType type,
            String description,
            Member initiatedBy,
            String idempotencyKey
    ) {
        MoneyTransfer transfer = new MoneyTransfer();
        transfer.bindFamily(family);
        transfer.sourceAccount = source;
        transfer.targetAccount = target;
        transfer.amount = amount;
        transfer.currency = family.getCurrency();
        transfer.type = type;
        transfer.status = TransferStatus.COMPLETED;
        transfer.description = description;
        transfer.initiatedBy = initiatedBy;
        transfer.idempotencyKey = idempotencyKey;
        transfer.completedAt = Instant.now();
        return transfer;
    }

    public Account getSourceAccount() {
        return sourceAccount;
    }

    public Account getTargetAccount() {
        return targetAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public TransferType getType() {
        return type;
    }

    public TransferStatus getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public Member getInitiatedBy() {
        return initiatedBy;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
