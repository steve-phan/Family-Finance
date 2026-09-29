package com.familyfinance.domain;

import com.familyfinance.domain.enums.Role;
import com.familyfinance.domain.enums.TransferRequestStatus;
import com.familyfinance.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransferRequestTest {

    private TransferRequest request;

    @BeforeEach
    void setUp() {
        Family family = new Family("Muller", "EUR");
        Member child = new Member(family, "child@example.com", "h", "Leo", "Muller", Role.CHILD);
        Member parent = new Member(family, "parent@example.com", "h", "Anna", "Muller", Role.PARENT);
        request = new TransferRequest(family, child, parent, new BigDecimal("8.00"), "lunch");
    }

    @Test
    void startsPending() {
        assertThat(request.getStatus()).isEqualTo(TransferRequestStatus.PENDING);
        assertThat(request.isPending()).isTrue();
    }

    @Test
    void approveSetsResolvedAt() {
        request.approve();
        assertThat(request.getStatus()).isEqualTo(TransferRequestStatus.APPROVED);
        assertThat(request.getResolvedAt()).isNotNull();
    }

    @Test
    void cannotApproveTwice() {
        request.approve();
        assertThatThrownBy(() -> request.reject()).isInstanceOf(BusinessException.class);
    }
}
