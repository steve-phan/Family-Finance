package com.familyfinance.repository;

import com.familyfinance.domain.AllowanceRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface AllowanceRuleRepository extends JpaRepository<AllowanceRule, Long> {

    List<AllowanceRule> findByFamily_IdAndActiveTrue(Long familyId);

    @Query("""
            SELECT r.id AS id, r.tenantId AS familyId
            FROM AllowanceRule r
            WHERE r.active = true AND r.nextRunAt <= :now
            """)
    List<DueAllowance> findDue(@Param("now") Instant now);
}
