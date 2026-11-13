package com.pep.toolshare.domain;

import java.util.Objects;

/**
 * Where a tool sits: an aisle letter and a bay number, written {@code A-01}. Two locations with the same aisle and
 * bay are equal, so a {@code ShelfLocation} works as a {@link java.util.HashMap} key, and locations sort by aisle,
 * then bay ({@code A-02} before {@code A-10} before {@code B-01}).
 */
public final class ShelfLocation implements Comparable<ShelfLocation> {
    private final char aisle;
    private final int bay;

    public ShelfLocation(char aisle, int bay) {
        if (aisle < 'A' || aisle > 'Z' || bay < 0 || bay > 99) {
            throw new IllegalArgumentException("not a shelf location: " + aisle + "-" + bay);
        }
        this.aisle = aisle;
        this.bay = bay;
    }

    /** Parses {@code A-01}; anything else throws {@link IllegalArgumentException}. */
    public static ShelfLocation parse(String text) {
        if (text == null || text.length() != 4 || text.charAt(1) != '-'
                || !Character.isDigit(text.charAt(2)) || !Character.isDigit(text.charAt(3))) {
            throw new IllegalArgumentException("not a shelf location: " + text);
        }
        return new ShelfLocation(text.charAt(0), Integer.parseInt(text.substring(2)));
    }

    public char aisle() {
        return aisle;
    }

    public int bay() {
        return bay;
    }

    @Override
    public boolean equals(Object other) {
        // TODO LEARNER 6A: equal when aisle and bay are equal (follow the equals contract).
        return super.equals(other);
    }

    @Override
    public int hashCode() {
        // TODO LEARNER 6A: equal locations must have equal hash codes.
        return super.hashCode();
    }

    /** Natural order: aisle first, then bay. */
    @Override
    public int compareTo(ShelfLocation other) {
        // TODO LEARNER 6A
        throw new UnsupportedOperationException("TODO LEARNER 6A: compareTo");
    }

    @Override
    public String toString() {
        return aisle + "-" + (bay < 10 ? "0" : "") + bay;
    }
}
