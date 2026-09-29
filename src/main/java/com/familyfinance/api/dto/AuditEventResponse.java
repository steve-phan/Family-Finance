package com.familyfinance.api.dto;

import com.familyfinance.domain.AuditEvent;
import com.familyfinance.domain.enums.AuditAction;

import java.time.Instant;

public record AuditEventResponse(
        Long id,
        Long actorId,
        AuditAction action,
        String entityType,
        Long entityId,
        String detail,
        Instant createdAt
) {
    public static AuditEventResponse from(AuditEvent event) {
        return new AuditEventResponse(
                event.getId(),
                event.getActor() == null ? null : event.getActor().getId(),
                event.getAction(),
                event.getEntityType(),
                event.getEntityId(),
                event.getDetail(),
                event.getCreatedAt()
        );
    }
}
