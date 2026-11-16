package com.pep.toolshare.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Reusable rules for choosing tools, expressed as {@link Predicate}s. Write each rule as an anonymous class that
 * implements {@code Predicate<Tool>}; Week 4 shows the shorter lambda form of the same idea.
 */
public final class ToolFilters {
    private ToolFilters() {
    }

    /** Tools whose status is {@code AVAILABLE}. */
    public static Predicate<Tool> available() {
        // TODO LEARNER 3G
        throw new UnsupportedOperationException("TODO LEARNER 3G: available");
    }

    /** Tools in {@code category}. */
    public static Predicate<Tool> inCategory(ToolCategory category) {
        // TODO LEARNER 3G
        throw new UnsupportedOperationException("TODO LEARNER 3G: inCategory");
    }

    /** Tools whose replacement cost is strictly below {@code cents}. */
    public static Predicate<Tool> cheaperThan(long cents) {
        // TODO LEARNER 3G
        throw new UnsupportedOperationException("TODO LEARNER 3G: cheaperThan");
    }

    /** The tools that pass {@code rule}, in their original order, in a new list. Use a loop. */
    public static List<Tool> select(List<Tool> tools, Predicate<Tool> rule) {
        // TODO LEARNER 3G
        throw new UnsupportedOperationException("TODO LEARNER 3G: select");
    }
}
