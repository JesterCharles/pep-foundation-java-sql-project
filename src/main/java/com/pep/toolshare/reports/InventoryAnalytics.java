package com.pep.toolshare.reports;

import com.pep.toolshare.domain.Loan;
import com.pep.toolshare.domain.ShelfLocation;
import com.pep.toolshare.domain.Tool;
import com.pep.toolshare.domain.ToolCategory;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Week 6 analysis of rows that are already loaded (from memory or from the database). Every method must be a stream
 * pipeline; do not write {@code for} or {@code while} loops in this class. Results never depend on the order of the
 * input lists.
 */
public final class InventoryAnalytics {
    private InventoryAnalytics() {
    }

    /** Tools keyed by code. A repeated code throws {@link IllegalStateException}. */
    public static Map<String, Tool> indexByCode(List<Tool> tools) {
        // TODO LEARNER 6C
        throw new UnsupportedOperationException("TODO LEARNER 6C: indexByCode");
    }

    /**
     * Number of loans per category, in {@link ToolCategory} declaration order. Categories without loans are left out;
     * loans of unknown tools are ignored.
     */
    public static Map<ToolCategory, Long> loansPerCategory(List<Loan> loans, List<Tool> tools) {
        // TODO LEARNER 6C
        throw new UnsupportedOperationException("TODO LEARNER 6C: loansPerCategory");
    }

    /**
     * The {@code limit} most-borrowed tools that have at least one loan: most loans first, ties by code. Loans of
     * unknown tools are ignored.
     */
    public static List<ToolUsage> mostBorrowed(List<Loan> loans, List<Tool> tools, int limit) {
        // TODO LEARNER 6C
        throw new UnsupportedOperationException("TODO LEARNER 6C: mostBorrowed");
    }

    /** The open loan that is most days late on {@code asOf} (ties: lowest id), or empty when none is late. */
    public static Optional<Loan> longestOverdue(List<Loan> loans, LocalDate asOf) {
        // TODO LEARNER 6C
        throw new UnsupportedOperationException("TODO LEARNER 6C: longestOverdue");
    }

    /** Total fees per member id, ordered by member id; every member who has a loan appears, even with 0. */
    public static Map<String, Long> feesByMember(List<Loan> loans) {
        // TODO LEARNER 6C
        throw new UnsupportedOperationException("TODO LEARNER 6C: feesByMember");
    }

    /** Tool codes grouped by shelf location, locations in natural order, codes sorted within each location. */
    public static Map<ShelfLocation, List<String>> toolCodesByShelf(List<Tool> tools) {
        // TODO LEARNER 6C
        throw new UnsupportedOperationException("TODO LEARNER 6C: toolCodesByShelf");
    }
}
