package com.pep.toolshare.basics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Week 2 building blocks for the Lantern Tool Library: plain static methods over numbers, strings, arrays, lists
 * and maps. Later milestones call these methods, so keep every signature unchanged.
 *
 * <p>Money is always a whole number of cents held in a {@code long}, never a {@code double}.
 */
public final class LendingMath {
    private LendingMath() {
    }

    /**
     * Late fee for a loan: {@code daysLate} times {@code dailyRateCents}, but never more than {@code capCents}.
     * A loan that is on time or early ({@code daysLate <= 0}) costs nothing.
     */
    public static long lateFeeCents(int daysLate, long dailyRateCents, long capCents) {
        // TODO LEARNER 2A: return the capped late fee (see docs/milestones/2-lending-basics.md).
        throw new UnsupportedOperationException("TODO LEARNER 2A: lateFeeCents");
    }

    /**
     * Membership tier name for a number of completed loans: 0-4 {@code STARTER}, 5-19 {@code REGULAR}, 20 or more
     * {@code CHAMPION}. A negative count is {@code INVALID}.
     */
    public static String membershipTier(int completedLoans) {
        // TODO LEARNER 2A: choose the tier with if/else or a switch.
        throw new UnsupportedOperationException("TODO LEARNER 2A: membershipTier");
    }

    /** Formats cents as dollars, for example {@code 123456} becomes {@code $1,234.56} and {@code 5} becomes {@code $0.05}. */
    public static String formatCents(long cents) {
        // TODO LEARNER 2B: build the dollar text with division, remainder and a loop.
        throw new UnsupportedOperationException("TODO LEARNER 2B: formatCents");
    }

    /**
     * Parses a dollar amount typed by staff into cents. Accepts surrounding spaces, an optional leading {@code $},
     * digits, and an optional {@code .} followed by one or two digits: {@code " $12.5 "} is {@code 1250},
     * {@code "7"} is {@code 700}. Anything else, including {@code null}, blank text or commas, returns {@code -1}.
     */
    public static long parseCents(String text) {
        // TODO LEARNER 2B: validate and convert the text character by character.
        throw new UnsupportedOperationException("TODO LEARNER 2B: parseCents");
    }

    /**
     * The check digit of a tool number: add its decimal digits, and keep adding the digits of the result until a
     * single digit remains. {@code 1009 -> 1+0+0+9 = 10 -> 1+0 = 1}. Must be written recursively.
     */
    public static int checkDigit(int number) {
        // TODO LEARNER 2C: base case for one digit, recursive step for more digits.
        throw new UnsupportedOperationException("TODO LEARNER 2C: checkDigit");
    }

    /**
     * Cleans a tool code typed by staff: strip outer spaces, upper-case it and remove every space inside it.
     * {@code " lt-1009- 1 "} becomes {@code "LT-1009-1"}. {@code null} becomes an empty string.
     */
    public static String normalizeToolCode(String raw) {
        // TODO LEARNER 2C: use strip, toUpperCase and a loop.
        throw new UnsupportedOperationException("TODO LEARNER 2C: normalizeToolCode");
    }

    /**
     * A valid tool code is exactly {@code LT-dddd-c}: the letters LT, a dash, four digits, a dash, and the check
     * digit of the four-digit number. {@code LT-1009-1} is valid; {@code LT-1009-2} and {@code lt-1009-1} are not.
     */
    public static boolean isValidToolCode(String code) {
        // TODO LEARNER 2C: check the shape, then compare the last character with checkDigit.
        throw new UnsupportedOperationException("TODO LEARNER 2C: isValidToolCode");
    }

    /** Binary search: the index of {@code target} in ascending {@code sortedNumbers}, or {@code -1}. */
    public static int indexOfToolNumber(int[] sortedNumbers, int target) {
        // TODO LEARNER 2D: narrow low and high until the target is found or they cross.
        throw new UnsupportedOperationException("TODO LEARNER 2D: indexOfToolNumber");
    }

    /** Reverses the array in place with two indexes that move toward each other (newest-first display). */
    public static void reverseInPlace(int[] values) {
        // TODO LEARNER 2D: swap values from both ends until the indexes meet.
        throw new UnsupportedOperationException("TODO LEARNER 2D: reverseInPlace");
    }

    /**
     * Codes whose matching {@code daysLate} entry is greater than zero, in their original order. The two arrays are
     * parallel: {@code daysLate[i]} belongs to {@code codes[i]}.
     */
    public static List<String> overdueCodes(String[] codes, int[] daysLate) {
        // TODO LEARNER 2E: collect matching codes into an ArrayList.
        throw new UnsupportedOperationException("TODO LEARNER 2E: overdueCodes");
    }

    /**
     * Counts how often each category name appears. Names are counted after {@code strip()} and
     * {@code toUpperCase()}; {@code null} and blank names are skipped.
     */
    public static Map<String, Integer> countByCategory(List<String> categories) {
        // TODO LEARNER 2E: count with a HashMap and getOrDefault.
        throw new UnsupportedOperationException("TODO LEARNER 2E: countByCategory");
    }
}
