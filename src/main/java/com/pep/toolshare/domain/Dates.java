package com.pep.toolshare.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Small date helpers so the domain code reads clearly. */
public final class Dates {
    private Dates() {
    }

    /** Whole days from {@code from} to {@code to}; negative when {@code to} is earlier. */
    public static int daysBetween(LocalDate from, LocalDate to) {
        return Math.toIntExact(ChronoUnit.DAYS.between(from, to));
    }

    /** Parses an ISO date such as {@code 2026-10-15}, or returns {@code null} when the text is not a valid date. */
    public static LocalDate parseOrNull(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(text.strip());
        } catch (java.time.format.DateTimeParseException exception) {
            return null;
        }
    }
}
