package com.familyfinance.service;

import com.familyfinance.api.dto.TransferResponse;
import com.familyfinance.domain.Account;
import com.familyfinance.domain.Member;
import com.familyfinance.domain.MoneyTransfer;
import com.familyfinance.domain.enums.AuditAction;
import com.familyfinance.domain.enums.TransferType;
import com.familyfinance.exception.BusinessException;
import com.familyfinance.exception.ErrorCode;
import com.familyfinance.repository.MoneyTransferRepository;
import com.familyfinance.tx.MoneyTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class TransferService {

    private final MoneyTransferRepository transferRepository;
    private final MemberLookup memberLookup;
    private final MoneyRules moneyRules;
    private final TransferTypeResolver transferTypeResolver;
    private final AuditService auditService;

    public TransferService(
            MoneyTransferRepository transferRepository,
            MemberLookup memberLookup,
            MoneyRules moneyRules,
            TransferTypeResolver transferTypeResolver,
            AuditService auditService
    ) {
        this.transferRepository = transferRepository;
        this.memberLookup = memberLookup;
        this.moneyRules = moneyRules;
        this.transferTypeResolver = transferTypeResolver;
        this.auditService = auditService;
    }

    @MoneyTransaction
    public TransferResponse send(Long senderId, Long recipientId, BigDecimal rawAmount, String description, String idempotencyKey) {
        if (senderId.equals(recipientId)) {
            throw new BusinessException(ErrorCode.SAME_ACCOUNT, "Cannot send money to yourself");
        }

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<MoneyTransfer> existing = transferRepository.findByInitiatedBy_IdAndIdempotencyKey(senderId, idempotencyKey);
            if (existing.isPresent()) {
                return TransferResponse.from(existing.get());
            }
        }

        Member sender = memberLookup.requireMember(senderId);
        Member recipient = memberLookup.requireMember(recipientId);
        TransferType type = transferTypeResolver.resolve(sender, recipient);
        MoneyTransfer saved = moveMoney(senderId, recipientId, sender, recipient, rawAmount, type, description, sender, blankToNull(idempotencyKey));
        auditService.record(
                sender.getFamily(),
                sender,
                AuditAction.TRANSFER_COMPLETED,
                "MoneyTransfer",
                saved.getId(),
                "%s sent %s %s to %s".formatted(sender.getDisplayName(), saved.getAmount(), saved.getCurrency(), recipient.getDisplayName())
        );
        return TransferResponse.from(saved);
    }

    @Transactional
    public MoneyTransfer sendTyped(
            Long senderId,
            Long recipientId,
            BigDecimal rawAmount,
            String description,
            TransferType type,
            Member initiatedBy
    ) {
        if (senderId.equals(recipientId)) {
            throw new BusinessException(ErrorCode.SAME_ACCOUNT, "Cannot send money to yourself");
        }
        Member sender = memberLookup.requireMember(senderId);
        Member recipient = memberLookup.requireMember(recipientId);
        MoneyTransfer saved = moveMoney(senderId, recipientId, sender, recipient, rawAmount, type, description, initiatedBy, null);
        auditService.record(
                sender.getFamily(),
                initiatedBy,
                AuditAction.TRANSFER_COMPLETED,
                "MoneyTransfer",
                saved.getId(),
                description
        );
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<TransferResponse> historyForMember(Long memberId, Pageable pageable) {
        return transferRepository.findInvolvingMember(memberId, pageable).map(TransferResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<TransferResponse> historyForFamily(Long familyId, Pageable pageable) {
        return transferRepository.findByFamily_IdOrderByCreatedAtDesc(familyId, pageable).map(TransferResponse::from);
    }

    private MoneyTransfer moveMoney(
            Long senderId,
            Long recipientId,
            Member sender,
            Member recipient,
            BigDecimal rawAmount,
            TransferType type,
            String description,
            Member initiatedBy,
            String idempotencyKey
    ) {
        BigDecimal amount = moneyRules.normalize(rawAmount);
        memberLookup.requireEnabled(sender);
        memberLookup.requireEnabled(recipient);
        memberLookup.requireSameFamily(sender, recipient);

        Long firstLock = Math.min(senderId, recipientId);
        Long secondLock = Math.max(senderId, recipientId);
        Account first = memberLookup.lockAccount(firstLock);
        Account second = memberLookup.lockAccount(secondLock);
        Account source = senderId.equals(firstLock) ? first : second;
        Account target = recipientId.equals(firstLock) ? first : second;

        source.debit(amount);
        target.credit(amount);

        return transferRepository.save(MoneyTransfer.completed(
                sender.getFamily(),
                source,
                target,
                amount,
                type,
                description,
                initiatedBy,
                idempotencyKey
        ));
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}
