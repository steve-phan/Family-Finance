package com.familyfinance.tenant;

import java.util.Optional;
import java.util.function.Supplier;

public final class TenantContext {

    private static final ThreadLocal<Long> CURRENT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static Optional<Long> current() {
        return Optional.ofNullable(CURRENT.get());
    }

    public static Long require() {
        return current().orElseThrow(() -> new IllegalStateException("No tenant is bound to this thread"));
    }

    public static boolean isBound() {
        return CURRENT.get() != null;
    }

    public static void requireMatches(Long familyId) {
        if (isBound() && !require().equals(familyId)) {
            throw new IllegalStateException("Tenant mismatch");
        }
    }

    public static void set(Long tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId is required");
        }
        CURRENT.set(tenantId);
    }

    public static void run(Long tenantId, Runnable action) {
        call(tenantId, () -> {
            action.run();
            return null;
        });
    }

    public static <T> T call(Long tenantId, Supplier<T> action) {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId is required");
        }
        Long previous = CURRENT.get();
        CURRENT.set(tenantId);
        try {
            return action.get();
        } finally {
            if (previous == null) {
                CURRENT.remove();
            } else {
                CURRENT.set(previous);
            }
        }
    }

    public static void clear() {
        CURRENT.remove();
    }
}
