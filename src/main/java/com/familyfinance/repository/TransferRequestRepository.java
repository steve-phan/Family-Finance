package com.familyfinance.repository;

import com.familyfinance.domain.TransferRequest;
import com.familyfinance.domain.enums.TransferRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransferRequestRepository extends JpaRepository<TransferRequest, Long> {

    List<TransferRequest> findByPayer_IdAndStatusOrderByCreatedAtDesc(Long payerId, TransferRequestStatus status);

    List<TransferRequest> findByRequester_IdOrderByCreatedAtDesc(Long requesterId);

    List<TransferRequest> findByFamily_IdOrderByCreatedAtDesc(Long familyId);
}
