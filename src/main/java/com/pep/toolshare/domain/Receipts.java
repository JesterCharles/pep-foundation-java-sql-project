package com.pep.toolshare.domain;

import com.pep.toolshare.basics.LendingMath;

import java.time.LocalDate;

/** Plain-text receipts printed at the lending desk. */
public final class Receipts {
    private Receipts() {
    }

    /**
     * A return receipt, built with a {@link StringBuilder}. Lines end with {@code \n}. Example:
     *
     * <pre>
     * Lantern Tool Library - return receipt
     * Member: M-0002 Bram Okafor (CHAMPION)
     * Tool: LT-1003-4 Cordless drill
     * Loaned: 2026-09-03  Due: 2026-09-10  Returned: 2026-09-14
     * Days late: 4
     * Policy: 2 grace days, then Standard: daily fee, capped at $20.00
     * Fee: $2.00
     * </pre>
     */
    public static String returnReceipt(Member member, Tool tool, Loan returnedLoan, FeePolicy policy) {
        // TODO LEARNER 3H: build the receipt with a StringBuilder.
        throw new UnsupportedOperationException("TODO LEARNER 3H: returnReceipt");
    }
}
