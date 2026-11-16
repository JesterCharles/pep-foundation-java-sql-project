package com.pep.toolshare.reports;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * A first-in-first-out wait list for one tool. Each entry may wait only once at a time. {@code null} entries are
 * rejected with {@link NullPointerException}.
 *
 * @param <T> what waits in line (member ids in this project)
 */
public final class WaitList<T> {
    // TODO LEARNER 6B: choose the collections that give first-in-first-out order and a fast "already waiting" check.

    /** Adds {@code entry} at the back; returns {@code false} (and changes nothing) when it is already waiting. */
    public boolean join(T entry) {
        // TODO LEARNER 6B
        throw new UnsupportedOperationException("TODO LEARNER 6B: join");
    }

    /** Removes and returns the entry at the front, or an empty {@link Optional} when nobody waits. */
    public Optional<T> next() {
        // TODO LEARNER 6B
        throw new UnsupportedOperationException("TODO LEARNER 6B: next");
    }

    /** The entry at the front without removing it. */
    public Optional<T> peek() {
        // TODO LEARNER 6B
        throw new UnsupportedOperationException("TODO LEARNER 6B: peek");
    }

    /** 1 for the front entry, 2 for the next, ...; 0 when {@code entry} is not waiting. */
    public int positionOf(T entry) {
        // TODO LEARNER 6B
        throw new UnsupportedOperationException("TODO LEARNER 6B: positionOf");
    }

    public int size() {
        // TODO LEARNER 6B
        throw new UnsupportedOperationException("TODO LEARNER 6B: size");
    }

    /**
     * The waiting entries, front first, as a copy taken at call time that cannot be modified: later changes to the
     * wait list do not change it.
     */
    public List<T> snapshot() {
        // TODO LEARNER 6B
        throw new UnsupportedOperationException("TODO LEARNER 6B: snapshot");
    }
}
