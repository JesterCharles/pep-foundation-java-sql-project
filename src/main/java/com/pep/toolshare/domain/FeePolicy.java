package com.pep.toolshare.domain;

import java.time.LocalDate;

/**
 * Decides the late fee for a loan. Implementations must never return a negative amount.
 *
 * <p>This is a functional interface: it has exactly one abstract method, so an anonymous class (or, from Week 4, a
 * lambda) can implement it.
 */
@FunctionalInterface
public interface FeePolicy {
    /** Late fee in cents for {@code loan}, whose tool belongs to {@code category}, judged on {@code asOf}. */
    long feeCents(Loan loan, ToolCategory category, LocalDate asOf);

    /** A short description shown on receipts. Implementations may override it. */
    default String describe() {
        return "Custom fee policy";
    }
}
