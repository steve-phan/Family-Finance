package com.familyfinance.service;

import com.familyfinance.api.dto.TransferResponse;
import com.familyfinance.config.TransferProperties;
import com.familyfinance.domain.Account;
import com.familyfinance.domain.Family;
import com.familyfinance.domain.Member;
import com.familyfinance.domain.MoneyTransfer;
import com.familyfinance.domain.enums.Role;
import com.familyfinance.exception.BusinessException;
import com.familyfinance.exception.ErrorCode;
import com.familyfinance.exception.InsufficientFundsException;
import com.familyfinance.repository.MoneyTransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private MoneyTransferRepository transferRepository;
    @Mock
    private MemberLookup memberLookup;
    @Mock
    private AuditService auditService;

    private TransferService transferService;
    private Member parent;
    private Member child;
    private Account parentAccount;
    private Account childAccount;

    @BeforeEach
    void setUp() {
        TransferProperties properties = new TransferProperties();
        properties.setMaxAmount(new BigDecimal("10000.00"));
        transferService = new TransferService(
                transferRepository,
                memberLookup,
                new MoneyRules(properties),
                new TransferTypeResolver(),
                auditService
        );

        Family family = new Family("Muller", "EUR");
        parent = new Member(family, "anna@example.com", "h", "Anna", "Muller", Role.PARENT);
        child = new Member(family, "leo@example.com", "h", "Leo", "Muller", Role.CHILD);
        parentAccount = new Account(parent, family, "EUR");
        childAccount = new Account(child, family, "EUR");
        parentAccount.credit(new BigDecimal("100.00"));
    }

    @Test
    void parentCanSendToChild() {
        stubMembers(1L, 2L);
        when(transferRepository.save(any(MoneyTransfer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransferResponse response = transferService.send(1L, 2L, new BigDecimal("20.00"), "pocket money", null);

        assertThat(parentAccount.getBalance()).isEqualByComparingTo("80.00");
        assertThat(childAccount.getBalance()).isEqualByComparingTo("20.00");
        assertThat(response.amount()).isEqualByComparingTo("20.00");
        assertThat(response.type().name()).isEqualTo("PARENT_TO_CHILD");
    }

    @Test
    void childrenCanSendToEachOther() {
        Member sibling = new Member(parent.getFamily(), "mia@example.com", "h", "Mia", "Muller", Role.CHILD);
        Account siblingAccount = new Account(sibling, parent.getFamily(), "EUR");
        childAccount.credit(new BigDecimal("15.00"));

        when(memberLookup.requireMember(2L)).thenReturn(child);
        when(memberLookup.requireMember(3L)).thenReturn(sibling);
        when(memberLookup.lockAccount(2L)).thenReturn(childAccount);
        when(memberLookup.lockAccount(3L)).thenReturn(siblingAccount);
        when(transferRepository.save(any(MoneyTransfer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        transferService.send(2L, 3L, new BigDecimal("5.00"), "shared snack", null);

        assertThat(childAccount.getBalance()).isEqualByComparingTo("10.00");
        assertThat(siblingAccount.getBalance()).isEqualByComparingTo("5.00");
    }

    @Test
    void rejectsSelfTransfer() {
        assertThatThrownBy(() -> transferService.send(1L, 1L, new BigDecimal("1.00"), null, null))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.SAME_ACCOUNT);
    }

    @Test
    void rejectsWhenBalanceTooLow() {
        stubMembers(1L, 2L);
        assertThatThrownBy(() -> transferService.send(1L, 2L, new BigDecimal("100.01"), null, null))
                .isInstanceOf(InsufficientFundsException.class);
        assertThat(parentAccount.getBalance()).isEqualByComparingTo("100.00");
        assertThat(childAccount.getBalance()).isEqualByComparingTo("0.00");
    }

    @Test
    void returnsExistingTransferForIdempotencyKey() {
        MoneyTransfer existing = MoneyTransfer.completed(
                parent.getFamily(), parentAccount, childAccount, new BigDecimal("3.00"),
                com.familyfinance.domain.enums.TransferType.PARENT_TO_CHILD, "dup", parent, "key-1"
        );
        when(transferRepository.findByInitiatedBy_IdAndIdempotencyKey(1L, "key-1")).thenReturn(Optional.of(existing));

        TransferResponse response = transferService.send(1L, 2L, new BigDecimal("3.00"), "dup", "key-1");

        assertThat(response.amount()).isEqualByComparingTo("3.00");
        assertThat(parentAccount.getBalance()).isEqualByComparingTo("100.00");
    }

    private void stubMembers(Long senderId, Long recipientId) {
        when(memberLookup.requireMember(senderId)).thenReturn(parent);
        when(memberLookup.requireMember(recipientId)).thenReturn(child);
        when(memberLookup.lockAccount(senderId)).thenReturn(parentAccount);
        when(memberLookup.lockAccount(recipientId)).thenReturn(childAccount);
    }
}
