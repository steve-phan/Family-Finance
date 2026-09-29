package com.familyfinance.service;

import com.familyfinance.api.dto.TransferRequestResponse;
import com.familyfinance.api.dto.TransferResponse;
import com.familyfinance.domain.Member;
import com.familyfinance.domain.MoneyTransfer;
import com.familyfinance.domain.TransferRequest;
import com.familyfinance.domain.enums.AuditAction;
import com.familyfinance.domain.enums.TransferRequestStatus;
import com.familyfinance.domain.enums.TransferType;
import com.familyfinance.exception.ForbiddenOperationException;
import com.familyfinance.exception.NotFoundException;
import com.familyfinance.repository.TransferRequestRepository;
import com.familyfinance.security.MemberPrincipal;
import com.familyfinance.tx.MoneyTransaction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransferRequestService {

    private final TransferRequestRepository requestRepository;
    private final MemberLookup memberLookup;
    private final MoneyRules moneyRules;
    private final TransferService transferService;
    private final AuditService auditService;

    public TransferRequestService(
            TransferRequestRepository requestRepository,
            MemberLookup memberLookup,
            MoneyRules moneyRules,
            TransferService transferService,
            AuditService auditService
    ) {
        this.requestRepository = requestRepository;
        this.memberLookup = memberLookup;
        this.moneyRules = moneyRules;
        this.transferService = transferService;
        this.auditService = auditService;
    }

    @Transactional
    public TransferRequestResponse create(MemberPrincipal principal, Long payerId, BigDecimal rawAmount, String message) {
        if (principal.getMemberId().equals(payerId)) {
            throw new ForbiddenOperationException("Cannot request money from yourself");
        }
        BigDecimal amount = moneyRules.normalize(rawAmount);
        Member requester = memberLookup.requireMember(principal.getMemberId());
        Member payer = memberLookup.requireMember(payerId);
        memberLookup.requireEnabled(requester);
        memberLookup.requireEnabled(payer);
        memberLookup.requireSameFamily(requester, payer);

        TransferRequest request = requestRepository.save(
                new TransferRequest(requester.getFamily(), requester, payer, amount, message)
        );
        auditService.record(
                requester.getFamily(),
                requester,
                AuditAction.REQUEST_CREATED,
                "TransferRequest",
                request.getId(),
                requester.getDisplayName() + " asked " + payer.getDisplayName() + " for " + amount
        );
        return TransferRequestResponse.from(request);
    }

    @MoneyTransaction
    public TransferResponse approve(MemberPrincipal principal, Long requestId) {
        TransferRequest request = requireInFamily(principal, requestId);
        if (!request.getPayer().getId().equals(principal.getMemberId())) {
            throw new ForbiddenOperationException("Only the payer can approve this request");
        }
        request.approve();
        MoneyTransfer transfer = transferService.sendTyped(
                request.getPayer().getId(),
                request.getRequester().getId(),
                request.getAmount(),
                "Request #" + request.getId(),
                TransferType.REQUEST_FULFILLMENT,
                request.getPayer()
        );
        auditService.record(
                request.getFamily(),
                request.getPayer(),
                AuditAction.REQUEST_APPROVED,
                "TransferRequest",
                request.getId(),
                "Approved"
        );
        return TransferResponse.from(transfer);
    }

    @Transactional
    public TransferRequestResponse reject(MemberPrincipal principal, Long requestId) {
        TransferRequest request = requireInFamily(principal, requestId);
        if (!request.getPayer().getId().equals(principal.getMemberId())) {
            throw new ForbiddenOperationException("Only the payer can reject this request");
        }
        request.reject();
        auditService.record(
                request.getFamily(),
                request.getPayer(),
                AuditAction.REQUEST_REJECTED,
                "TransferRequest",
                request.getId(),
                "Rejected"
        );
        return TransferRequestResponse.from(request);
    }

    @Transactional
    public TransferRequestResponse cancel(MemberPrincipal principal, Long requestId) {
        TransferRequest request = requireInFamily(principal, requestId);
        if (!request.getRequester().getId().equals(principal.getMemberId())) {
            throw new ForbiddenOperationException("Only the requester can cancel this request");
        }
        request.cancel();
        auditService.record(
                request.getFamily(),
                request.getRequester(),
                AuditAction.REQUEST_CANCELLED,
                "TransferRequest",
                request.getId(),
                "Cancelled"
        );
        return TransferRequestResponse.from(request);
    }

    @Transactional(readOnly = true)
    public List<TransferRequestResponse> incomingPending(MemberPrincipal principal) {
        return requestRepository.findByPayer_IdAndStatusOrderByCreatedAtDesc(
                principal.getMemberId(),
                TransferRequestStatus.PENDING
        ).stream().map(TransferRequestResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<TransferRequestResponse> outgoing(MemberPrincipal principal) {
        return requestRepository.findByRequester_IdOrderByCreatedAtDesc(principal.getMemberId()).stream()
                .map(TransferRequestResponse::from)
                .toList();
    }

    private TransferRequest requireInFamily(MemberPrincipal principal, Long requestId) {
        TransferRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("TransferRequest", requestId));
        if (!request.getFamily().getId().equals(principal.getFamilyId())) {
            throw new ForbiddenOperationException("Request is not in your family");
        }
        return request;
    }
}
