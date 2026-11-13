package com.pep.toolshare.reports;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Returned tools are stacked in a cart and shelved last-in-first-out: the tool dropped most recently is on top.
 */
public final class ReturnCart {
    // TODO LEARNER 6B: choose the collection for a last-in-first-out stack.

    /** Puts a tool code on top of the cart. {@code null} is rejected. */
    public void drop(String toolCode) {
        // TODO LEARNER 6B
        throw new UnsupportedOperationException("TODO LEARNER 6B: drop");
    }

    /** The code on top without removing it, or {@code null} when the cart is empty. */
    public String peekTop() {
        // TODO LEARNER 6B
        throw new UnsupportedOperationException("TODO LEARNER 6B: peekTop");
    }

    /** Removes and returns the code on top; throws {@link NoSuchElementException} when the cart is empty. */
    public String takeTop() {
        // TODO LEARNER 6B
        throw new UnsupportedOperationException("TODO LEARNER 6B: takeTop " + NoSuchElementException.class);
    }

    public int size() {
        // TODO LEARNER 6B
        throw new UnsupportedOperationException("TODO LEARNER 6B: size");
    }

    /** The codes from top to bottom, as a list that cannot be modified. */
    public List<String> topToBottom() {
        // TODO LEARNER 6B
        throw new UnsupportedOperationException("TODO LEARNER 6B: topToBottom");
    }
}
