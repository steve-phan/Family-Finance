package com.familyfinance.domain;

import com.familyfinance.domain.enums.Role;
import com.familyfinance.exception.InsufficientFundsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountTest {

    private Account account;

    @BeforeEach
    void setUp() {
        Family family = new Family("Muller", "EUR");
        Member member = new Member(family, "parent@example.com", "hash", "Anna", "Muller", Role.PARENT);
        account = new Account(member, family, "EUR");
    }

    @Test
    void startsAtZero() {
        assertThat(account.getBalance()).isEqualByComparingTo("0.00");
    }

    @Test
    void creditIncreasesBalance() {
        account.credit(new BigDecimal("25.50"));
        assertThat(account.getBalance()).isEqualByComparingTo("25.50");
    }

    @Test
    void debitDecreasesBalance() {
        account.credit(new BigDecimal("40.00"));
        account.debit(new BigDecimal("12.25"));
        assertThat(account.getBalance()).isEqualByComparingTo("27.75");
    }

    @Test
    void debitRejectsInsufficientFunds() {
        account.credit(new BigDecimal("5.00"));
        assertThatThrownBy(() -> account.debit(new BigDecimal("5.01")))
                .isInstanceOf(InsufficientFundsException.class);
        assertThat(account.getBalance()).isEqualByComparingTo("5.00");
    }

    @Test
    void rejectsNonPositiveAmounts() {
        assertThatThrownBy(() -> account.credit(BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> account.debit(new BigDecimal("-1")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
