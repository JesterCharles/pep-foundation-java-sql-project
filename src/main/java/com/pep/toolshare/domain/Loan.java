package com.pep.toolshare.domain;

import java.time.LocalDate;
import java.util.Objects;

/**
 * One loan of one tool to one member. A record: its fields are final, and "changing" a loan means creating a new
 * one with {@link #returned(LocalDate, long)} or {@link #withId(long)}.
 *
 * <p>Rules checked by the compact constructor: {@code id >= 0} (0 means "not saved yet"); member id, tool code,
 * loan date and due date are required; the due date is not before the loan date; the return date is either
 * {@code null} (still out) or not before the loan date; the fee is not negative. A {@code null} required value
 * throws {@link NullPointerException}; any other broken rule throws {@link IllegalArgumentException}.
 */
public record Loan(long id, String memberId, String toolCode, LocalDate loanedOn, LocalDate dueOn,
                   LocalDate returnedOn, long feeCents) {

    public Loan {
        // TODO LEARNER 3E: validate the components (see the class comment).
    }

    /** A new, unsaved loan (id 0, not returned, no fee). */
    public static Loan open(String memberId, String toolCode, LocalDate loanedOn, LocalDate dueOn) {
        return new Loan(0, memberId, toolCode, loanedOn, dueOn, null, 0);
    }

    /** True while the tool has not come back. */
    public boolean isOpen() {
        // TODO LEARNER 3E
        throw new UnsupportedOperationException("TODO LEARNER 3E: isOpen");
    }

    /**
     * Whole days late, never negative. A returned loan is judged on its return date; an open loan on {@code asOf}.
     * Use {@link Dates#daysBetween(LocalDate, LocalDate)}.
     */
    public int daysLate(LocalDate asOf) {
        // TODO LEARNER 3E
        throw new UnsupportedOperationException("TODO LEARNER 3E: daysLate");
    }

    /**
     * A copy of this loan marked returned on {@code on} with {@code fee}. A loan that is already returned throws
     * {@link IllegalStateException}.
     */
    public Loan returned(LocalDate on, long fee) {
        // TODO LEARNER 3E
        throw new UnsupportedOperationException("TODO LEARNER 3E: returned");
    }

    /** A copy of this loan with a new id. */
    public Loan withId(long newId) {
        // TODO LEARNER 3E
        throw new UnsupportedOperationException("TODO LEARNER 3E: withId");
    }
}
