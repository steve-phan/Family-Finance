package com.familyfinance.repository;

import com.familyfinance.domain.MoneyTransfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MoneyTransferRepository extends JpaRepository<MoneyTransfer, Long> {

    Page<MoneyTransfer> findByFamily_IdOrderByCreatedAtDesc(Long familyId, Pageable pageable);

    @Query("""
            SELECT t FROM MoneyTransfer t
            WHERE t.sourceAccount.member.id = :memberId
               OR t.targetAccount.member.id = :memberId
            ORDER BY t.createdAt DESC
            """)
    Page<MoneyTransfer> findInvolvingMember(@Param("memberId") Long memberId, Pageable pageable);

    Optional<MoneyTransfer> findByInitiatedBy_IdAndIdempotencyKey(Long initiatedById, String idempotencyKey);
}
