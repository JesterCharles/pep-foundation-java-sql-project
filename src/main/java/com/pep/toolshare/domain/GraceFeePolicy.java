package com.pep.toolshare.domain;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Adds grace days in front of another policy: the loan is charged as if it had been due {@code graceDays} later.
 * It wraps (holds and delegates to) any other {@link FeePolicy} instead of extending one. Grace days must be zero
 * or more and the wrapped policy is required.
 */
public class GraceFeePolicy implements FeePolicy {
    private final int graceDays;
    private final FeePolicy wrapped;

    public GraceFeePolicy(int graceDays, FeePolicy wrapped) {
        // TODO LEARNER 3F
        throw new UnsupportedOperationException("TODO LEARNER 3F: GraceFeePolicy constructor");
    }

    @Override
    public long feeCents(Loan loan, ToolCategory category, LocalDate asOf) {
        // TODO LEARNER 3F
        throw new UnsupportedOperationException("TODO LEARNER 3F: feeCents");
    }

    /** {@code 2 grace days, then <wrapped description>} */
    @Override
    public String describe() {
        // TODO LEARNER 3F
        throw new UnsupportedOperationException("TODO LEARNER 3F: describe");
    }
}
