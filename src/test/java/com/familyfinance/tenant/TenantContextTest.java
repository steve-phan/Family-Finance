package com.familyfinance.tenant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TenantContextTest {

    @AfterEach
    void clear() {
        TenantContext.clear();
    }

    @Test
    void callRestoresPreviousTenant() {
        TenantContext.set(1L);
        Long nested = TenantContext.call(2L, TenantContext::require);
        assertThat(nested).isEqualTo(2L);
        assertThat(TenantContext.require()).isEqualTo(1L);
    }

    @Test
    void requireFailsWhenUnbound() {
        assertThatThrownBy(TenantContext::require).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rootResolverWhenUnbound() {
        FamilyTenantResolver resolver = new FamilyTenantResolver();
        assertThat(resolver.resolveCurrentTenantIdentifier()).isEqualTo(FamilyTenantResolver.ROOT);
        assertThat(resolver.isRoot(FamilyTenantResolver.ROOT)).isTrue();
        TenantContext.set(9L);
        assertThat(resolver.resolveCurrentTenantIdentifier()).isEqualTo(9L);
        assertThat(resolver.isRoot(9L)).isFalse();
    }
}
