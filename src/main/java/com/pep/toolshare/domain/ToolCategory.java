package com.pep.toolshare.domain;

import java.time.LocalDate;

/**
 * The library's tool categories. Each constant carries its display label, its late fee per day in cents and the
 * longest loan in days:
 *
 * <pre>
 * HAND      "Hand tools"        25  14
 * POWER     "Power tools"      100   7
 * GARDEN    "Garden"            50  10
 * LADDERS   "Ladders and access" 75   7
 * CLEANING  "Cleaning"          40   7
 * </pre>
 */
public enum ToolCategory {
    // TODO LEARNER 3B: give every constant its label, daily fee and maximum loan days, add the fields, a constructor
    // and the methods below.
    HAND, POWER, GARDEN, LADDERS, CLEANING;

    public String label() {
        throw new UnsupportedOperationException("TODO LEARNER 3B: label");
    }

    public long dailyFeeCents() {
        throw new UnsupportedOperationException("TODO LEARNER 3B: dailyFeeCents");
    }

    public int maxLoanDays() {
        throw new UnsupportedOperationException("TODO LEARNER 3B: maxLoanDays");
    }

    public LocalDate dueDate(LocalDate loanedOn) {
        throw new UnsupportedOperationException("TODO LEARNER 3B: dueDate");
    }

    public static ToolCategory fromCode(String code) {
        throw new UnsupportedOperationException("TODO LEARNER 3B: fromCode");
    }
}
