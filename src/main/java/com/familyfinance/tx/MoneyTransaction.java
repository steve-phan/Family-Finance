package com.familyfinance.tx;

import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Transactional(isolation = Isolation.READ_COMMITTED, timeout = 10, rollbackFor = Exception.class)
public @interface MoneyTransaction {

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            isolation = Isolation.READ_COMMITTED,
            timeout = 10,
            rollbackFor = Exception.class
    )
    @interface Isolated {
    }
}
