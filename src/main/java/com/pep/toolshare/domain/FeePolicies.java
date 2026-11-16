package com.pep.toolshare.domain;

import java.time.LocalDate;

/**
 * Static factory for fee policies. Callers ask for "the policy for this tier" and never need to know which classes
 * build it.
 *
 * <ul>
 *   <li>{@code STARTER}: the standard policy.</li>
 *   <li>{@code REGULAR}: 1 grace day, then the standard policy.</li>
 *   <li>{@code CHAMPION}: 2 grace days, then the standard policy.</li>
 * </ul>
 */
public final class FeePolicies {
    private FeePolicies() {
    }

    public static FeePolicy forTier(MemberTier tier) {
        // TODO LEARNER 3F
        throw new UnsupportedOperationException("TODO LEARNER 3F: forTier");
    }

    /** A policy that never charges, written as an anonymous class. Its description is the interface default. */
    public static FeePolicy waived() {
        // TODO LEARNER 3F
        throw new UnsupportedOperationException("TODO LEARNER 3F: waived");
    }
}
