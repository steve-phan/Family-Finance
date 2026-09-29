package com.familyfinance.service;

import com.familyfinance.domain.Family;
import com.familyfinance.domain.Member;
import com.familyfinance.domain.enums.Role;
import com.familyfinance.domain.enums.TransferType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TransferTypeResolverTest {

    private final TransferTypeResolver resolver = new TransferTypeResolver();
    private final Family family = new Family("Muller", "EUR");

    @Test
    void parentToChild() {
        assertThat(resolver.resolve(parent("a"), child("b"))).isEqualTo(TransferType.PARENT_TO_CHILD);
    }

    @Test
    void childToChild() {
        assertThat(resolver.resolve(child("a"), child("b"))).isEqualTo(TransferType.CHILD_TO_CHILD);
    }

    @Test
    void childToParent() {
        assertThat(resolver.resolve(child("a"), parent("b"))).isEqualTo(TransferType.CHILD_TO_PARENT);
    }

    @Test
    void parentToParent() {
        assertThat(resolver.resolve(parent("a"), parent("b"))).isEqualTo(TransferType.PARENT_TO_PARENT);
    }

    private Member parent(String key) {
        return new Member(family, key + "@p.example", "h", "P", key, Role.PARENT);
    }

    private Member child(String key) {
        return new Member(family, key + "@c.example", "h", "C", key, Role.CHILD);
    }
}
