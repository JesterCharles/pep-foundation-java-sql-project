package com.pep.toolshare.domain;

import com.pep.toolshare.basics.LendingMath;

import java.time.LocalDate;

/**
 * The standard late fee: the category's daily fee for every day late, capped at {@code capCents}. Reuse
 * {@link LendingMath#lateFeeCents(int, long, long)}. The cap must be zero or more.
 */
public class StandardFeePolicy implements FeePolicy {
    /** The library-wide cap: $20.00. */
    public static final long DEFAULT_CAP_CENTS = 2_000;

    private final long capCents;

    public StandardFeePolicy(long capCents) {
        // TODO LEARNER 3F
        throw new UnsupportedOperationException("TODO LEARNER 3F: StandardFeePolicy constructor");
    }

    public StandardFeePolicy() {
        this(DEFAULT_CAP_CENTS);
    }

    @Override
    public long feeCents(Loan loan, ToolCategory category, LocalDate asOf) {
        // TODO LEARNER 3F
        throw new UnsupportedOperationException("TODO LEARNER 3F: feeCents");
    }

    /** {@code Standard: daily fee, capped at $20.00} (the amount comes from the cap). */
    @Override
    public String describe() {
        // TODO LEARNER 3F
        throw new UnsupportedOperationException("TODO LEARNER 3F: describe");
    }
}
