package com.familyfinance.service;

import com.familyfinance.api.dto.AuditEventResponse;
import com.familyfinance.domain.AuditEvent;
import com.familyfinance.domain.Family;
import com.familyfinance.domain.Member;
import com.familyfinance.domain.enums.AuditAction;
import com.familyfinance.repository.AuditEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditEventRepository auditEventRepository;

    public AuditService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void record(Family family, Member actor, AuditAction action, String entityType, Long entityId, String detail) {
        auditEventRepository.save(new AuditEvent(family, actor, action, entityType, entityId, detail));
    }

    @Transactional(readOnly = true)
    public Page<AuditEventResponse> list(Long familyId, Pageable pageable) {
        return auditEventRepository.findByFamily_IdOrderByCreatedAtDesc(familyId, pageable).map(AuditEventResponse::from);
    }
}
