package com.familyfinance.repository;

import com.familyfinance.domain.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {

    Page<AuditEvent> findByFamily_IdOrderByCreatedAtDesc(Long familyId, Pageable pageable);
}
