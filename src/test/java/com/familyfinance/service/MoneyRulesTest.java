package com.familyfinance.service;

import com.familyfinance.config.TransferProperties;
import com.familyfinance.exception.BusinessException;
import com.familyfinance.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyRulesTest {

    private MoneyRules moneyRules;

    @BeforeEach
    void setUp() {
        TransferProperties properties = new TransferProperties();
        properties.setMaxAmount(new BigDecimal("100.00"));
        moneyRules = new MoneyRules(properties);
    }

    @Test
    void normalizesScale() {
        assertThat(moneyRules.normalize(new BigDecimal("10"))).isEqualByComparingTo("10.00");
    }

    @Test
    void rejectsZero() {
        assertThatThrownBy(() -> moneyRules.normalize(BigDecimal.ZERO))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_AMOUNT);
    }

    @Test
    void rejectsMoreThanTwoDecimals() {
        assertThatThrownBy(() -> moneyRules.normalize(new BigDecimal("1.234")))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void rejectsAboveMax() {
        assertThatThrownBy(() -> moneyRules.normalize(new BigDecimal("100.01")))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.AMOUNT_TOO_LARGE);
    }
}
